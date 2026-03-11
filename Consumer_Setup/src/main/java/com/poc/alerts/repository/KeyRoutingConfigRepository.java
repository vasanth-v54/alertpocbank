package com.poc.alerts.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.poc.alerts.entity.KeyRoutingConfig;

import java.util.Optional;

public interface KeyRoutingConfigRepository extends JpaRepository<KeyRoutingConfig, Long> {

    Optional<KeyRoutingConfig> findByMessageTypeAndAlertTypeAndIsActive(
            String messageType,
            String alertType,
            Boolean isActive
    );

}