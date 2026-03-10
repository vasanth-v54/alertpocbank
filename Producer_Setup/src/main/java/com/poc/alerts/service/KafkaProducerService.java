package com.poc.alerts.service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.poc.alerts.constants.AppConstants;

@Service
public class KafkaProducerService {

	private static final Logger log = LoggerFactory.getLogger(KafkaProducerService.class);
	private static final Logger auditLog =
	        LoggerFactory.getLogger("KAFKA_AUDIT_LOGGER");

	private final KafkaTemplate<String, String> kafkaTemplate;

	public KafkaProducerService(KafkaTemplate<String, String> kafkaTemplate) {
		this.kafkaTemplate = kafkaTemplate;
	}

	public void sendPayload(Long payloadId, String payloadType, String payloadJson) throws JsonMappingException, JsonProcessingException {

        log.info("Producing message to topic {}", AppConstants.TOPIC);
        log.debug("Payload JSON {}", payloadJson);
        

        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(payloadJson);

        // Extract fields
        String businessKey = rootNode.path("businessKey").asText();
        String eventType = rootNode.path("eventType").asText();
        String messageType =
        		rootNode.path("payload")
                    .path("customFieldDetails")
                    .path("MessageType")
                    .asText();

        System.out.println("MessageType = " + messageType);
        System.out.println("BusinessKey : " + businessKey);
        System.out.println("EventType   : " + eventType);
        
        ProducerRecord<String, String> record =
                new ProducerRecord<>(AppConstants.TOPIC, businessKey, payloadJson);

        // Add headers
        record.headers().add("event-type", eventType.getBytes(StandardCharsets.UTF_8));
        record.headers().add("alert-type", messageType.getBytes(StandardCharsets.UTF_8));

        System.out.println("---- HEADER DEBUG ----");
        record.headers().forEach(header ->
                System.out.println(header.key() + " = " + new String(header.value())));
        System.out.println("----------------------");

        kafkaTemplate.send(record)
                .whenComplete((result, ex) -> {

                    if (ex == null && result != null) {

                        String topic = result.getRecordMetadata().topic();
                        int partition = result.getRecordMetadata().partition();
                        long offset = result.getRecordMetadata().offset();

                        Instant timestamp = Instant.now();

                        log.info("Message successfully published to topic {}", topic);

                        auditLog.info(
                                "EVENT=KAFKA_PUBLISHED | timestamp={} | payloadId={} | payloadType={} | topic={} | partition={} | offset={} | payload={}",
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
                                "EVENT=KAFKA_PUBLISH_FAILED | payloadId={} | payloadType={} | topic={} | error={} | payload={}",
                                payloadId,
                                payloadType,
                                AppConstants.TOPIC,
                                ex != null ? ex.getMessage() : "unknown",
                                payloadJson
                        );
                    }
                });
    }

}