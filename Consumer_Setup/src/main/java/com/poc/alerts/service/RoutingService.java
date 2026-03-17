package com.poc.alerts.service;

import org.springframework.stereotype.Service;

import com.poc.alerts.entity.KeyRoutingConfig;
import com.poc.alerts.repository.KeyRoutingConfigRepository;

@Service
public class RoutingService {
	private final KeyRoutingConfigRepository repository;

	public RoutingService(KeyRoutingConfigRepository repository) {
		this.repository = repository;
	}

	public KeyRoutingConfig getRoutingConfig(String messageType, String alertType) {

		return repository.findFirstByMessageTypeAndAlertTypeAndIsActive(messageType, alertType, true)
				.orElseThrow(() -> new RuntimeException("Routing configuration not found"));
	}
}