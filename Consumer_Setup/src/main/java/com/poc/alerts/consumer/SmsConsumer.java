package com.poc.alerts.consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.poc.alerts.header.HeaderExtractor;
import com.poc.alerts.repository.ProcessedAlertAuditRepository;
import com.poc.alerts.service.AuditService;
import com.poc.alerts.service.TemplateProcessorService;
import com.poc.alerts.util.PayloadParser;

@Component
public class SmsConsumer {

    private static final Logger log =
            LoggerFactory.getLogger(SmsConsumer.class);

    private static final Logger templateLog =
            LoggerFactory.getLogger("TEMPLATE_PROCESS_LOGGER");

    @Autowired
    private ProcessedAlertAuditRepository processedAlertAuditRepository;
    @Autowired
    private AuditService auditService;

    @Autowired
    private TemplateProcessorService templateProcessorService;

    @KafkaListener(
            topics="notifications.events",
            groupId="sms-consumer-group")
    public void consume(String message, ConsumerRecord<String,String> record) {

        log.info("=====================================================");
        log.info("SMS CONSUMER TRIGGERED");
        log.info("Kafka message received from topic: notifications.events");

        try {

            // HEADER VALIDATION
            String eventTypeHeader = HeaderExtractor.extractHeader(record, "event-type");
            String alertTypeHeader = HeaderExtractor.extractHeader(record, "alert-type");

            log.info("Header EventType : {}", eventTypeHeader);
            log.info("Header AlertType : {}", alertTypeHeader);

            if (!"sms".equalsIgnoreCase(alertTypeHeader)) {

                log.info("Message not meant for SMS consumer. Skipping.");
                log.info("=====================================================");
                return;
            }

            String type = PayloadParser.extractType(message);

            log.info("Extracted MessageType from payload: {}", type);

            if(!"SMS".equalsIgnoreCase(type) &&
               !"BOTH".equalsIgnoreCase(type)) {

                log.info("MessageType is not SMS/BOTH → skipping SMS consumer");
                log.info("=====================================================");
                return;
            }

            log.info("Message eligible for SMS processing");

            // STEP 1 AUDIT
            log.info("Saving message to AUDIT table");

            auditService.saveAudit("notifications.events", message);

            log.info("Audit record successfully inserted");
            
            String eventId = PayloadParser.extractEventId(message);
            type = "SMS";

            boolean alreadyProcessed =
                    processedAlertAuditRepository
                    .existsByEventIdAndMessageType(eventId, type);

            if (alreadyProcessed) {

                log.info("Duplicate event detected for EventId : {} and MessageType : {}",
                        eventId, type);

                return;
            }

            // STEP 2 EXTRACT FIELDS
            log.info("Extracting event details from payload");

            String eventType = PayloadParser.extractEventType(message);
            String alertType = PayloadParser.extractAlertType(message);

            log.info("EventType : {}", eventType);
            log.info("AlertType : {}", alertType);

            // STEP 3 TEMPLATE PROCESS
            log.info("Starting SMS template processing");

            String result =
                    templateProcessorService.processTemplate(
                            message,
                            eventType,
                            alertType,
                            "SMS");

            templateLog.info("Generated SMS Message:");
            templateLog.info(result);

            log.info("SMS template generated successfully");

            log.info("SMS notification processing completed");

        }
        catch(Exception e){

            log.error("Error occurred while processing SMS notification", e);
        }

        log.info("=====================================================");
    }
}