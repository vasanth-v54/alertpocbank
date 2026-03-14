package com.poc.alerts.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class Simple1TestConfig {
    
    @Bean
    public String testBean() {
        return "Test Bean Initialized";
    }
}
