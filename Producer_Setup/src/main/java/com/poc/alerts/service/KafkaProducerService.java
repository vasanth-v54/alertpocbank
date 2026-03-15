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
import com.poc.alerts.entity.DltLog;
import com.poc.alerts.strategy.impl.FileDltLoggingStrategy;
import com.poc.alerts.util.DltLoggerUtil;

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

        String businessKey = headerNode.path("businessKey").asText(null);
        String eventType   = headerNode.path("eventType").asText(null);
        String eventId     = headerNode.path("eventId").asText(null);
        String source      = headerNode.path("eventSourceId").asText(null);
        String status      = headerNode.path("status").asText(null);

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
         * Add Kafka Headers (SAFE)
         * -----------------------------
         */

        String alertType = getMessageType(payloadJson);
        log.info("payload alertType: {}", alertType);

        addHeaderIfPresent(record, "event-type", eventType);
        addHeaderIfPresent(record, "event-id", eventId);
        addHeaderIfPresent(record, "source", source);
        addHeaderIfPresent(record, "status", status);
        addHeaderIfPresent(record, "alert-type", alertType);

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
                                record.headers(),
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
                                record.headers(),
                                payloadId,
                                payloadType,
                                AppConstants.TOPIC,
                                ex != null ? ex.getMessage() : "unknown",
                                payloadJson
                        );

                        /*
                         * -----------------------------------------
                         * PRODUCER DLT LOG
                         * -----------------------------------------
                         */

                        try {

                            DltLog dltLog = DltLoggerUtil.build(
                                    "ProducerService",
                                    "500",
                                    record.headers().toString(),
                                    eventId,
                                    ex != null ? ex.getMessage() : "Unknown error",
                                    payloadJson
                            );

                            new FileDltLoggingStrategy().log(dltLog);

                        } catch (Exception dltEx) {

                            log.error("DLT logging failed", dltEx);
                        }
                    }
                });
    }

    /*
     * -----------------------------------------
     * Safe Header Method
     * -----------------------------------------
     */

    private void addHeaderIfPresent(ProducerRecord<String, String> record,
                                    String key,
                                    String value) {

        if (value != null && !value.trim().isEmpty()) {

            record.headers().add(
                    key,
                    value.getBytes(StandardCharsets.UTF_8)
            );
        }
    }

    /*
     * -----------------------------------------
     * Extract MessageType
     * -----------------------------------------
     */

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
}