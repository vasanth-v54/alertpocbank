package com.consumer.config;

import com.azure.messaging.eventhubs.*;
import com.azure.messaging.eventhubs.checkpointstore.blob.*;
import com.azure.messaging.eventhubs.models.ErrorContext;
import com.azure.messaging.eventhubs.models.EventContext;
import com.azure.storage.blob.*;

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

        auditLogger.info("\n=======================================");
        auditLogger.info("📥 EVENT RECEIVED");
        auditLogger.info("Partition  : " + context.getPartitionContext().getPartitionId());
        auditLogger.info("Offset     : " + context.getEventData().getOffset());
        auditLogger.info("Sequence   : " + context.getEventData().getSequenceNumber());
        auditLogger.info("Payload    : " + payload);
        auditLogger.info("=======================================\n");

        // checkpoint (VERY IMPORTANT)
        context.updateCheckpoint();
    }

    private void processError(ErrorContext errorContext) {
        System.err.println("ERROR: " + errorContext.getThrowable());
    }
}
