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
import com.poc.alerts.repository.PayloadRepository;
import com.poc.alerts.strategy.impl.FileDltLoggingStrategy;
import com.poc.alerts.util.DltLoggerUtil;

@Service
public class KafkaProducerService {

    private static final Logger log =
            LoggerFactory.getLogger(KafkaProducerService.class);

    private static final Logger auditLog =
            LoggerFactory.getLogger("KAFKA_AUDIT_LOGGER");

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final PayloadRepository payloadRepository;

    private final ObjectMapper mapper = new ObjectMapper();

    public KafkaProducerService(KafkaTemplate<String, String> kafkaTemplate,
                                PayloadRepository payloadRepository) {

        this.kafkaTemplate = kafkaTemplate;
        this.payloadRepository = payloadRepository;
    }

    public void sendPayload(Long payloadId,
                            String payloadType,
                            String headerJson,
                            String payloadJson) {

        log.info("Producing message to topic {}", AppConstants.TOPIC);

        JsonNode headerNode;

        /*
         * -----------------------------------------
         * JSON PARSE VALIDATION
         * -----------------------------------------
         */

        try {

            headerNode = mapper.readTree(headerJson);

        } catch (Exception e) {

            log.error("Invalid JSON payload", e);

            writeDlt(payloadId,
                    "UNKNOWN_EVENT_ID",
                    "Invalid JSON payload",
                    payloadJson);

            return;
        }

        /*
         * -----------------------------------------
         * EXTRACT VALUES SAFELY
         * -----------------------------------------
         */

        String businessKey = getSafeText(headerNode, "businessKey");
        String eventType   = getSafeText(headerNode, "eventType");
        String eventId     = getSafeText(headerNode, "eventId");
        String source      = getSafeText(headerNode, "eventSourceId");
        String status      = getSafeText(headerNode, "status");

        log.info("BusinessKey : {}", businessKey);
        log.info("EventType   : {}", eventType);
        log.info("EventId     : {}", eventId);

        /*
         * -----------------------------------------
         * VALIDATION
         * -----------------------------------------
         */

        if (eventId == null || eventId.trim().isEmpty()) {

            writeDlt(payloadId,
                    "UNKNOWN_EVENT_ID",
                    "Payload validation failed: Missing or Empty eventId",
                    payloadJson);

            return;
        }

        if (businessKey == null || businessKey.trim().isEmpty()) {

            writeDlt(payloadId,
                    eventId,
                    "Payload validation failed: Missing businessKey",
                    payloadJson);

            return;
        }

        /*
         * -----------------------------------------
         * CREATE KAFKA RECORD
         * -----------------------------------------
         */

        ProducerRecord<String, String> record =
                new ProducerRecord<>(AppConstants.TOPIC, businessKey, payloadJson);

        String alertType = getMessageType(payloadJson);

        addHeader(record, "event-type", eventType);
        addHeader(record, "event-id", eventId);
        addHeader(record, "source", source);
        addHeader(record, "status", status);
        addHeader(record, "alert-type", alertType);

        record.headers().forEach(header ->
                log.info("{} = {}", header.key(),
                        new String(header.value()))
        );

        /*
         * -----------------------------------------
         * SEND TO KAFKA
         * -----------------------------------------
         */

        kafkaTemplate.send(record)
                .whenComplete((result, ex) -> {

                    if (ex == null && result != null) {

                        Instant timestamp = Instant.now();

                        auditLog.info(
                                "EVENT=KAFKA_PUBLISHED | payloadId={} | topic={} | partition={} | offset={} | timestamp={}",
                                payloadId,
                                result.getRecordMetadata().topic(),
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset(),
                                timestamp
                        );

                        payloadRepository.updateTopicStatus(payloadId, "SUCCESS");

                    } else {

                        log.error("Kafka publish failed", ex);

                        writeDlt(payloadId,
                                eventId,
                                ex != null ? ex.getMessage() : "Kafka publish error",
                                payloadJson);
                    }
                });
    }

    /*
     * -----------------------------------------
     * SAFE JSON VALUE EXTRACTION
     * -----------------------------------------
     */

    private String getSafeText(JsonNode node, String field) {

        JsonNode valueNode = node.get(field);

        if (valueNode == null || valueNode.isNull()) {
            return null;
        }

        String value = valueNode.asText();

        if (value == null || value.trim().isEmpty() || "null".equalsIgnoreCase(value.trim())) {
            return null;
        }

        return value;
    }

    /*
     * -----------------------------------------
     * WRITE DLT + UPDATE DB
     * -----------------------------------------
     */

    private void writeDlt(Long payloadId,
                          String eventId,
                          String errorMessage,
                          String payloadJson) {

        try {

            DltLog dltLog = DltLoggerUtil.build(
                    "ProducerService",
                    "400",
                    "payload",
                    eventId,
                    errorMessage,
                    payloadJson
            );

            new FileDltLoggingStrategy().log(dltLog);

            payloadRepository.updateTopicStatus(payloadId, "FAILED");

        } catch (Exception e) {

            log.error("Failed writing DLT log", e);
        }
    }

    /*
     * -----------------------------------------
     * SAFE HEADER ADD
     * -----------------------------------------
     */

    private void addHeader(ProducerRecord<String, String> record,
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
     * GET MESSAGE TYPE
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