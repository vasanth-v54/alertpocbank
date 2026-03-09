package com.poc.alerts.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.poc.alerts.entity.EventTemplateMapping;

public interface EventTemplateMappingRepository
        extends JpaRepository<EventTemplateMapping, Long>{

    Optional<EventTemplateMapping>
        findFirstByEventUniqueIdAndAlertChannelAndEnabledTrueOrderByPriorityAsc(
                String eventUniqueId,
                String alertChannel);
}