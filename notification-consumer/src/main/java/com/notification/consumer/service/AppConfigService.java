package com.notification.consumer.service;

import java.util.Set;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.notification.consumer.entity.AppConfig;
import com.notification.consumer.repository.AppConfigRepository;

@Service
public class AppConfigService {

	private final AppConfigRepository repository;

	public AppConfigService(AppConfigRepository repository) {
		this.repository = repository;
	}

	@Cacheable(value = "configCache", key = "#key")
	public String getValue(String key) {
		return repository.findByConfigKeyAndIsActiveTrue(key).map(AppConfig::getConfigValue).orElse(null);
	}

	public Integer getInt(String key) {
		String value = getValue(key);
		return value != null ? Integer.parseInt(value) : null;
	}

	public Boolean getBoolean(String key) {
		String value = getValue(key);
		return value != null ? Boolean.parseBoolean(value) : null;
	}
	
	
	private final ObjectMapper mapper = new ObjectMapper();

    public Set<String> getMaskFields() {
        try {
            AppConfig config = repository
                    .findByConfigKeyAndIsActive("MASK_FIELDS", 1)
                    .orElseThrow(() -> new RuntimeException("MASK_FIELDS config not found"));

            return mapper.readValue(config.getConfigValue(), Set.class);

        } catch (Exception e) {
            throw new RuntimeException("Error reading MASK_FIELDS config", e);
        }
    }
}