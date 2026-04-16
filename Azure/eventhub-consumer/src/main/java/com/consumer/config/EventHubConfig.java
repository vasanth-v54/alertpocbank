package com.consumer.config;

import com.azure.messaging.eventhubs.*;
import com.azure.messaging.eventhubs.checkpointstore.blob.*;
import com.azure.messaging.eventhubs.models.ErrorContext;
import com.azure.messaging.eventhubs.models.EventContext;
import com.azure.storage.blob.*;
import com.consumer.service.LdgApiService;
import com.consumer.service.PayloadAuditService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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

    public EventHubConfig(PayloadAuditService payloadAuditService,
                      LdgApiService ldgApiService) {
        this.payloadAuditService = payloadAuditService;
        this.ldgApiService = ldgApiService;
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
            payloadAuditService.saveAudit(payload, partition, offset, sequence);
        } catch (Exception e) {
            auditLogger.error("❌ DB SAVE FAILED", e);
        }

        ldgApiService.callLdgApi(payload);
        // checkpoint (VERY IMPORTANT)
        context.updateCheckpoint();
    }

    private void processError(ErrorContext errorContext) {
        System.err.println("ERROR: " + errorContext.getThrowable());
    }
}
