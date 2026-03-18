package com.poc.alerts.service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.poc.alerts.constants.AppConstants;

@Service
public class KafkaProducerService {

    private static final Logger log = LoggerFactory.getLogger(KafkaProducerService.class);

    private static final Logger auditLog =
            LoggerFactory.getLogger("KAFKA_AUDIT_LOGGER");

    private final KafkaTemplate<String, String> kafkaTemplate;

    private final ObjectMapper mapper = new ObjectMapper();

    public KafkaProducerService(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendPayload(Long payloadId,
                            String payloadType,
                            String headerJson,
                            String payloadJson) throws Exception {

        log.info("Producing message to topic {}", AppConstants.TOPIC);

        /*
         * -----------------------------
         * Parse header JSON
         * -----------------------------
         */
        JsonNode headerNode = mapper.readTree(headerJson);

        String businessKey = headerNode.path("businessKey").asText();
        String eventType   = headerNode.path("eventType").asText();
        String eventId     = headerNode.path("eventId").asText();
        String source      = headerNode.path("eventSourceId").asText();
        String status      = headerNode.path("status").asText();

        log.info("BusinessKey : {}", businessKey);
        log.info("EventType   : {}", eventType);
        log.info("EventId     : {}", eventId);

        /*
         * -----------------------------
         * Create Kafka Record
         * -----------------------------
         */

        ProducerRecord<String, String> record =
                new ProducerRecord<>(AppConstants.TOPIC, businessKey, payloadJson);

        /*
         * -----------------------------
         * Add Kafka Headers
         * -----------------------------
         */
        
        String messageType=getMessageType(payloadJson);
        String alertType=getAlertType(payloadJson);
        log.info("payload alertType: {}, messageType: {}",messageType);
        
        record.headers().add("event-type", eventType.getBytes(StandardCharsets.UTF_8));
        record.headers().add("event-id", eventId.getBytes(StandardCharsets.UTF_8));
        record.headers().add("MessageType", messageType.getBytes(StandardCharsets.UTF_8));
        record.headers().add("status", status.getBytes(StandardCharsets.UTF_8));
        record.headers().add("alert-type", alertType.getBytes(StandardCharsets.UTF_8));

        /*
         * -----------------------------
         * Debug Headers
         * -----------------------------
         */

        log.info("----- Kafka Headers -----");

        record.headers().forEach(header ->
                log.info("{} = {}", header.key(), new String(header.value()))
        );

        log.info("-------------------------");

        /*
         * -----------------------------
         * Send to Kafka
         * -----------------------------
         */

        kafkaTemplate.send(record)
                .whenComplete((result, ex) -> {

                    if (ex == null && result != null) {

                        String topic = result.getRecordMetadata().topic();
                        int partition = result.getRecordMetadata().partition();
                        long offset = result.getRecordMetadata().offset();

                        Instant timestamp = Instant.now();

                        log.info("Message successfully published to topic {}", topic);

                        auditLog.info(
                                "HEADERS={} | EVENT=KAFKA_PUBLISHED | timestamp={} | payloadId={} | payloadType={} | topic={} | partition={} | offset={} | payload={}",
                                record,
                                timestamp,
                                payloadId,
                                payloadType,
                                topic,
                                partition,
                                offset,
                                payloadJson
                        );

                    } else {

                        log.error("Kafka publish failed for payloadId={}", payloadId, ex);

                        auditLog.error(
                                "HEADERS={} | EVENT=KAFKA_PUBLISH_FAILED | payloadId={} | payloadType={} | topic={} | error={} | payload={}",
                                record,
                                payloadId,
                                payloadType,
                                AppConstants.TOPIC,
                                ex != null ? ex.getMessage() : "unknown",
                                payloadJson
                        );
                    }
                });
    }
    
    public static String getMessageType(String json) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(json);

            return root
                    .path("customFieldDetails")
                    .path("MessageType")
                    .asText();

        } catch (Exception e) {
            return null;
        }
    }
    
    public static String getAlertType(String json) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(json);

            return root
                    .path("customFieldDetails")
                    .path("alertType")
                    .asText();

        } catch (Exception e) {
            return null;
        }
    }

}