package com.notification.consumer.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.notification.consumer.dto.ValidationResult;
import com.notification.consumer.entity.RoutingKeyConfig;
import com.notification.consumer.repository.RoutingKeyConfigRepository;
import com.notification.consumer.util.JsonValidator;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoutingKeyConfigService {

	private final RoutingKeyConfigRepository repository;
	private static final Logger log = LoggerFactory.getLogger(RoutingKeyConfigService.class);
	/*public RoutingKeyConfigService(RoutingKeyConfigRepository repository) {
		super();
		this.repository = repository;
	}*/

	public RoutingKeyConfig findMatchingConfig(String payload) {

		List<RoutingKeyConfig> configs = repository.findByIsActiveTrue();

		for (RoutingKeyConfig config : configs) {

			try {

				ValidationResult result = JsonValidator.validate(config.getTemplateIdentifiers(), payload);

				if (result.isSuccess()) {
					log.info("PASS :"+config);
					return config;
				} else {
					log.info("FAIL: " + result.getMessage());
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return null;
	}
}