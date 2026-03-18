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

        try {

            JsonNode headerNode = mapper.readTree(headerJson);

        /*
         EXTRACT HEADER VALUES
         */

            String businessKey = getSafeText(headerNode, "businessKey");
            String eventType   = getSafeText(headerNode, "eventType");
            String eventId     = getSafeText(headerNode, "eventId");
            String source      = getSafeText(headerNode, "eventSourceId");
            String status      = getSafeText(headerNode, "status");
            String alertType   = getSafeText(headerNode, "alert-type"); // Read from headers

            log.info("BusinessKey : {}", businessKey);
            log.info("EventType   : {}", eventType);
            log.info("EventId     : {}", eventId);
            log.info("AlertType   : {}", alertType);

        /*
         CREATE KAFKA RECORD
         */

            ProducerRecord<String, String> record =
                    new ProducerRecord<>(AppConstants.TOPIC, businessKey, payloadJson);

            addHeader(record, "event-type", eventType);
            addHeader(record, "event-id", eventId);
            addHeader(record, "source", source);
            addHeader(record, "status", status);
            addHeader(record, "alert-type", alertType); // Add to record

            record.headers().forEach(header ->
                    log.info("{} = {}",
                            header.key(),
                            new String(header.value()))
            );

        /*
         SEND MESSAGE
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

                            payloadRepository.updateTopicStatus(payloadId, "PUBLISHED");

                        } else {

                            log.error("Kafka publish failed", ex);

                            writeDlt(payloadId,
                                    eventId,
                                    ex != null ? ex.getMessage() : "Kafka publish error",
                                    payloadJson);
                        }
                    });

        } catch (Exception e) {

            log.error("Producer error", e);

            writeDlt(payloadId,
                    "UNKNOWN_EVENT_ID",
                    e.getMessage(),
                    payloadJson);
        }
    }

/*
 SAFE JSON TEXT EXTRACTION
 */

    private String getSafeText(JsonNode node, String field) {

        JsonNode valueNode = node.get(field);

        if (valueNode == null || valueNode.isNull()) {
            return null;
        }

        return valueNode.asText();
    }

/*
 WRITE DLT + UPDATE DB
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
 ADD HEADER
 */

    private void addHeader(ProducerRecord<String, String> record,
                           String key,
                           String value) {

        // Kafka header values cannot be null. Convert null to an empty string
        // to ensure the header is always present for consumer-side validation.
        String headerValue = (value == null) ? "" : value;
        record.headers().add(
                key,
                headerValue.getBytes(StandardCharsets.UTF_8)
        );
    }
}
