package com.consumer.config;

import com.azure.messaging.eventhubs.*;
import com.azure.messaging.eventhubs.checkpointstore.blob.*;
import com.azure.messaging.eventhubs.models.ErrorContext;
import com.azure.messaging.eventhubs.models.EventContext;
import com.azure.storage.blob.*;
import com.consumer.service.ConfigService;
import com.consumer.service.LdgApiService;
import com.consumer.service.PayloadAuditService;


import java.util.List;

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
            log.info("eventType :: "+eventType);
            // Get application
            String application = JsonSearchUtil.findFirstValue(root, "application");
            
            //  Fetch config
            List<String> allowedEvents =
                    configService.getValuesAsList("LDG_ALLOWED_EVENT_TYPE");
            log.info("allowedEvents :: "+allowedEvents);
            String allowedApps =
                    configService.getValue("LDG_ALLOWED_APPLICATION");
            log.info("application :: "+application);
            log.info("allowedApps :: "+allowedApps);
            boolean allowedApplicationCheck=application.equalsIgnoreCase(allowedApps);
            boolean allowedEventTypeCheck=isAllowed(eventType, allowedEvents);
            log.info("allowedApplicationCheck :: "+allowedApplicationCheck);
            log.info("allowedEventTypeCheck :: "+allowedEventTypeCheck);
            if(allowedApplicationCheck && allowedEventTypeCheck){
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
        System.err.println("ERROR: " + errorContext.getThrowable());
    }

    private boolean isAllowed(String value, List<String> allowedList) {

        if (value == null || allowedList == null) return false;
            return allowedList.stream().anyMatch(v -> v.equalsIgnoreCase(value.trim()));
    }
}
