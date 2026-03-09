package com.poc.alerts.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.poc.alerts.entity.EventTypeMaster;

public interface EventTypeMasterRepository
        extends JpaRepository<EventTypeMaster, Long>{

    Optional<EventTypeMaster>
        findByEventTypeAndAlertTypeAndEnabledTrue(
                String eventType,
                String alertType);
}