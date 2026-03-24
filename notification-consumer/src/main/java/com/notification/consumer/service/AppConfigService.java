package com.notification.consumer.service;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

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
}