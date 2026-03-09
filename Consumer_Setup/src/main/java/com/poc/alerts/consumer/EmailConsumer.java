package com.poc.alerts.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.poc.alerts.repository.ProcessedAlertAuditRepository;
import com.poc.alerts.service.AuditService;
import com.poc.alerts.service.TemplateProcessorService;
import com.poc.alerts.util.PayloadParser;

@Component
public class EmailConsumer {

    private static final Logger log =
            LoggerFactory.getLogger(EmailConsumer.class);

    @Autowired
    private AuditService auditService;

    @Autowired
    private TemplateProcessorService templateProcessorService;
    
    @Autowired
    private ProcessedAlertAuditRepository processedAlertAuditRepository;

    @KafkaListener(
            topics = "notifications.events",
            groupId = "email-consumer-group")
    public void consume(String message) {

        log.info("--------------------------------------------------");
        log.info("Kafka message received for EMAIL processing");

        try {

            String messageType = PayloadParser.extractType(message);

            log.info("Extracted MessageType from payload: {}", messageType);

            if (!"EMAIL".equalsIgnoreCase(messageType)
                    && !"BOTH".equalsIgnoreCase(messageType)) {

            	log.info("MessageType is not EMAIL/BOTH. Skipping message");
                return;
            }

            log.info("Saving consumer audit entry");

            auditService.saveAudit("notifications.events", message);

            log.info("Audit entry successfully stored");
            
            String eventId = PayloadParser.extractEventId(message);
            messageType = "EMAIL";   // or SMS depending consumer

            boolean alreadyProcessed =
                    processedAlertAuditRepository
                    .existsByEventIdAndMessageType(eventId, messageType);

            if (alreadyProcessed) {

                log.info("Duplicate event detected for EventId : {} and MessageType : {}",
                        eventId, messageType);

                return;
            }

            // extract values from payload
            String eventType = PayloadParser.extractEventType(message);
            String alertType = PayloadParser.extractAlertType(message);

            log.info("EventType extracted: {}", eventType);
            log.info("AlertType extracted: {}", alertType);

            log.info("Starting template processing for EMAIL");

            templateProcessorService.processTemplate(
                    message,
                    eventType,
                    alertType,
                    "EMAIL"
            );

            log.info("Template processing completed successfully");

        } catch (Exception ex) {

        	log.error("Error occurred while processing EMAIL notification", ex);
        }

        log.info("EMAIL processing completed");
        log.info("--------------------------------------------------");

       
    }
}	