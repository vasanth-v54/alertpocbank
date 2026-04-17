package com.consumer.config;

import com.azure.messaging.eventhubs.*;
import com.azure.messaging.eventhubs.checkpointstore.blob.*;
import com.azure.messaging.eventhubs.models.ErrorContext;
import com.azure.messaging.eventhubs.models.EventContext;
import com.azure.storage.blob.*;
import com.consumer.service.ConfigService;
import com.consumer.service.LdgApiService;
import com.consumer.service.PayloadAuditService;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.consumer.util.JsonSearchUtil;

@Configuration
public class EventHubConfig {

    @Value("${azure.eventhub.connection-string}")
    private String connectionString;

    @Value("${azure.eventhub.name}")
    private String eventHubName;

    @Value("${azure.eventhub.consumer-group}")
    private String consumerGroup;

    @Value("${azure.storage.connection-string}")
    private String blobConnectionString;

    @Value("${azure.storage.container-name}")
    private String containerName;
    
    private static final Logger auditLogger =
            LoggerFactory.getLogger("AUDIT_LOGGER");

    private final PayloadAuditService payloadAuditService;

    private final LdgApiService ldgApiService;

    private final ConfigService configService;

    private static final Logger log = LoggerFactory.getLogger(EventHubConfig.class);

    public EventHubConfig(PayloadAuditService payloadAuditService,
                      LdgApiService ldgApiService,ConfigService configService) {
        this.payloadAuditService = payloadAuditService;
        this.ldgApiService = ldgApiService;
        this.configService=configService;
    }

    @Bean
    public EventProcessorClient processorClient() {

        BlobContainerAsyncClient containerClient =
                new BlobContainerClientBuilder()
                        .connectionString(blobConnectionString)
                        .containerName(containerName)
                        .buildAsyncClient();

        return new EventProcessorClientBuilder()
                .connectionString(connectionString, eventHubName)
                .consumerGroup(consumerGroup)
                .processEvent(this::processEvent)
                .processError(this::processError)
                .checkpointStore(new BlobCheckpointStore(containerClient))
                .buildEventProcessorClient();
    }

    private void processEvent(EventContext context) {

        String payload = context.getEventData().getBodyAsString();

        Integer partition = null;
        try {
            partition = Integer.parseInt(
                    context.getPartitionContext().getPartitionId()
            );
        } catch (Exception ignored) {}

        Long offset = context.getEventData().getOffset();
        Long sequence = context.getEventData().getSequenceNumber();

        auditLogger.info("\n=======================================");
        auditLogger.info("📥 EVENT RECEIVED");
        auditLogger.info("Partition  : " + partition);
        auditLogger.info("Offset     : " + offset);
        auditLogger.info("Sequence   : " + sequence);
        auditLogger.info("Payload    : " + payload);
        auditLogger.info("=======================================\n");

        // ✅ SAVE TO DB
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.readTree(payload);

            // Get eventType
            String eventType = JsonSearchUtil.findFirstValue(root, "eventType");
           
            // Get application
            String application = JsonSearchUtil.findFirstValue(root, "application");
            
            // Get MessageType
            String MessageType = JsonSearchUtil.findFirstValue(root, "MessageType");

            // Get Alert Type
            String AlertType = JsonSearchUtil.findFirstValue(root, "AlertType");

            //  Fetch config
            List<String> allowedEvents =
                    configService.getValuesAsList("LDG_ALLOWED_EVENT_TYPE");
            log.info("eventType :: "+eventType);
            log.info("allowedEvents :: "+allowedEvents);

            List<String> allowedApps =
                    configService.getValuesAsList("LDG_ALLOWED_APPLICATION");
            log.info("application :: "+application);
            log.info("allowedApps :: "+allowedApps);

            List<String> allowedMessageType =
                    configService.getValuesAsList("LDG_ALLOWED_MESSAGE_TYPE");
            log.info("MessageType :: "+MessageType);
            log.info("allowedMessageType :: "+allowedMessageType);

            List<String> allowedAlertType =
                    configService.getValuesAsList("LDG_ALERT_TYPE");
            log.info("AlertType :: "+AlertType);
            log.info("allowedAlertType :: "+allowedAlertType);

            boolean allowedApplicationCheck=isAllowedApp(application,allowedApps);
            boolean allowedEventTypeCheck=isAllowed(eventType, allowedEvents);
            boolean allowedMessageTypeCheck=isAllowed(MessageType, allowedMessageType);
            boolean allowedAlertTypeCheck=isAllowed(AlertType, allowedAlertType); 

            log.info("allowedApplicationCheck :: "+allowedApplicationCheck);
            log.info("allowedEventTypeCheck :: "+allowedEventTypeCheck);
            log.info("allowedMessageTypeCheck :: "+allowedMessageTypeCheck);
            log.info("allowedAlertTypeCheck :: "+allowedAlertTypeCheck);

            if(allowedApplicationCheck && allowedEventTypeCheck && allowedAlertTypeCheck && allowedMessageTypeCheck){
                payloadAuditService.saveAudit(payload, partition, offset, sequence,"ACCEPTED");
                // API CALL
                ldgApiService.callLdgApi(payload);
            }else{
                payloadAuditService.saveAudit(payload, partition, offset, sequence,"REJECTED");
            }
        } catch (Exception e) {
            auditLogger.error("❌ DB SAVE FAILED", e);
        }

        
        // checkpoint (VERY IMPORTANT)
        context.updateCheckpoint();
    }

    private void processError(ErrorContext errorContext) {
        log.error("ERROR: " + errorContext.getThrowable());
    }

    private boolean isAllowed(String value, List<String> allowedList) {

        if (value == null || allowedList == null) return false;

        String input = normalize(value);

        return allowedList.stream()
                .filter(Objects::nonNull)
                .flatMap(v -> Arrays.stream(v.split("\\|")))
                .map(this::normalize)
                .anyMatch(v -> v.equalsIgnoreCase(input));
    }

    private String normalize(String str) {
        return str.trim().replaceAll("\\s*,\\s*", ",");
    }

    private boolean isAllowedApp(String value, List<String> allowedList) {

    if (value == null || allowedList == null) return false;

    String input = normalize(value);

    for (String item : allowedList) {

        if (item == null) continue;

        String[] parts = item.split("\\|");

        for (String part : parts) {

            String normalizedPart = normalize(part);

            // DEBUG LOG (very important)
            log.info("INPUT  : [" + input + "]");
            log.info("ALLOWED: [" + normalizedPart + "]");

            if (normalizedPart.equalsIgnoreCase(input)) {
                return true;
            }
        }
    }

    return false;
}
}
