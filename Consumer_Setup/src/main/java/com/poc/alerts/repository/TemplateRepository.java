package com.poc.alerts.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.poc.alerts.entity.TemplateMst;

public interface TemplateRepository extends JpaRepository<TemplateMst, Long> {

    Optional<TemplateMst> findByAlertTypeAndMessageTypeAndTemplateStatus(
            String alertType,
            String messageType,
            String templateStatus
    );
}