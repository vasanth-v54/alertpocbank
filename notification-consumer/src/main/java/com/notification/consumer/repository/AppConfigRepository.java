package com.notification.consumer.repository;


import org.springframework.data.jpa.repository.JpaRepository;

import com.notification.consumer.entity.AppConfig;

import java.util.Optional;

public interface AppConfigRepository extends JpaRepository<AppConfig, Long> {

    Optional<AppConfig> findByConfigKeyAndIsActiveTrue(String configKey);
    
    Optional<AppConfig> findByConfigKeyAndIsActive(String configKey, Integer isActive);
    
    
}