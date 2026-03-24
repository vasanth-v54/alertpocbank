package com.notification.consumer.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.notification.consumer.entity.RoutingKeyConfig;
import com.notification.consumer.repository.RoutingKeyConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RoutingKeyConfigService {

    private final RoutingKeyConfigRepository repository;
    public RoutingKeyConfigService(RoutingKeyConfigRepository repository) {
		super();
		this.repository = repository;
	}

	private final ObjectMapper objectMapper = new ObjectMapper();

    public RoutingKeyConfig findMatchingConfig(String eventType, String messageType) {

        List<RoutingKeyConfig> configs = repository.findByIsActiveTrue();

        for (RoutingKeyConfig config : configs) {

            try {
                JsonNode json = objectMapper.readTree(config.getTemplateIdentifiers());

                String configEventType = json.path("eventType").asText();
                String configMessageType = json.path("messageType").asText();

                if (eventType.equalsIgnoreCase(configEventType)
                        && messageType.equalsIgnoreCase(configMessageType)) {

                    return config;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }
}