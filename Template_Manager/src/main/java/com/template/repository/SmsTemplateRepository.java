package com.template.repository;

import com.template.entity.SmsTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SmsTemplateRepository extends JpaRepository<SmsTemplate, Integer> {
    boolean existsByTemplateName(String templateName);

    Optional<SmsTemplate> findByTemplateName(String templateName);

}