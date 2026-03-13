package com.poc.alerts.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.poc.alerts.entity.TemplateMst;

import java.util.Optional;

public interface TemplateRepository extends JpaRepository<TemplateMst,Long> {

    Optional<TemplateMst> findByMessageTypeAndAlertTypeAndIsActive(
            String messageType,
            String alertType,
            Boolean isActive
    );

}