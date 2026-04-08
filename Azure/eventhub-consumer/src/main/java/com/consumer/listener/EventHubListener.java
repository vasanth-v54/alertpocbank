package com.consumer.listener;



import com.azure.messaging.eventhubs.EventProcessorClient;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;

@Component
public class EventHubListener {

    private final EventProcessorClient client;

    public EventHubListener(EventProcessorClient client) {
        this.client = client;
    }

    @PostConstruct
    public void start() {
        client.start();
        System.out.println("🚀 Consumer started...");
    }

    @PreDestroy
    public void stop() {
        client.stop();
    }
}