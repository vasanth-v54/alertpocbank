package com.producer.service;

import com.azure.messaging.eventhubs.*;
import com.producer.entity.PayloadMst;
import com.producer.repository.PayloadRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class EventHubProducerService {

    @Value("${azure.eventhub.connection-string}")
    private String connectionString;

    @Value("${azure.eventhub.name}")
    private String eventHubName;

    private final PayloadRepository payloadRepository;

    private EventHubProducerClient producerClient;

    public EventHubProducerService(PayloadRepository payloadRepository) {
        this.payloadRepository = payloadRepository;
    }
    
    private static final Logger auditLogger =
            LoggerFactory.getLogger("AUDIT_LOGGER");
    
    @PostConstruct
    public void init() {

        producerClient = new EventHubClientBuilder()
                .connectionString(connectionString, eventHubName)
                .buildProducerClient();

        System.out.println("✅ Producer initialized");
    }

    public void sendPendingEvents() {

        // 🔥 FETCH PENDING RECORDS
        List<PayloadMst> payloads =
                payloadRepository.findByTopicStatusAndIsActive("PENDING", true);

        System.out.println("Pending records count: " + payloads.size());

        if (payloads.isEmpty()) {
            return;
        }

        EventDataBatch batch = producerClient.createBatch();

        for (PayloadMst payloadMst : payloads) {

            String payload = payloadMst.getPayload();

            EventData event = new EventData(payload);

            if (!batch.tryAdd(event)) {
                producerClient.send(batch);
                batch = producerClient.createBatch();
                batch.tryAdd(event);
            }

            // 🔥 UPDATE STATUS
            auditLogger.info("✅ Events sent & status updated "+payload);
            payloadMst.setTopicStatus("SENT");
        }

        producerClient.send(batch);

        // 🔥 SAVE STATUS
        payloadRepository.saveAll(payloads);
        
        System.out.println("✅ Events sent & status updated");
    }

    @PreDestroy
    public void close() {
        producerClient.close();
    }
}