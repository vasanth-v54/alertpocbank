package com.poc.alerts.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.poc.alerts.entity.EventTemplateMapping;

public interface EventTemplateMappingRepository
        extends JpaRepository<EventTemplateMapping, Long> {

    EventTemplateMapping findByTemplateId(String templateId);

}