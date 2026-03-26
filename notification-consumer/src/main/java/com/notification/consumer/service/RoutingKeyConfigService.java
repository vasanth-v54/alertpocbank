package com.notification.consumer.service;

import java.util.ArrayList;
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

	public List<RoutingKeyConfig> findMatchingConfigs(String payload) {

	    List<RoutingKeyConfig> configs = repository.findByIsActiveTrue();
	    List<RoutingKeyConfig> matchedConfigs = new ArrayList<>();

	    for (RoutingKeyConfig config : configs) {

	        try {
	            ValidationResult result =
	                    JsonValidator.validate(config.getTemplateIdentifiers(), payload);

	            if (result.isSuccess()) {
	                log.info("PASS: {}", config);
	                matchedConfigs.add(config); // ✅ collect instead of return
	            } else {
	                log.info("FAIL: {}", result.getMessage());
	            }

	        } catch (Exception e) {
	            log.error("Error processing config: {}", config, e);
	        }
	    }

	    return matchedConfigs; // ✅ return all matches
	}
}