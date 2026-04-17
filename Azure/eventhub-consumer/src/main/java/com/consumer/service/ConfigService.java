package com.consumer.service;

import com.consumer.entity.EwbConfigMtb;
import com.consumer.repository.EwbConfigRepository;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
public class ConfigService {

    private final EwbConfigRepository configRepo;

    public ConfigService(EwbConfigRepository configRepo) {
        this.configRepo = configRepo;
    }

    // 🔹 Get single value
    public String getValue(String key) {
        return configRepo.findByKeyAndIsActive(key, true)
                .map(EwbConfigMtb::getValue)
                .orElse(null);
    }

    /**
     * 🔹 Get pipe-separated values as list
     * IMPORTANT:
     * - Do NOT split by comma (,)
     * - Each entry is a FULL composite value: APP_CODE,EVENT_TYPE
     * - Split ONLY by pipe (|)
     */
    public List<String> getValuesAsList(String key) {

        String value = getValue(key);

        if (value == null || value.isBlank()) {
            return Collections.emptyList();
        }

        return Arrays.stream(value.split("\\|"))   // ✅ correct delimiter
                .map(String::trim)                 // clean spaces
                .filter(v -> !v.isEmpty())         // avoid empty values
                .toList();
    }
}