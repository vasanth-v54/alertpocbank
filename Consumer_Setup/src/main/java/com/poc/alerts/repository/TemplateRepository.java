package com.poc.alerts.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.poc.alerts.entity.TemplateMst;

public interface TemplateRepository
        extends JpaRepository<TemplateMst, Long> {

    TemplateMst findByAlertTypeAndMessageType(
            String alertType,
            String messageType
    );
}