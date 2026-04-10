package com.template.repository;

import com.template.entity.SmsTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SmsTemplateRepository extends JpaRepository<SmsTemplate, Integer> {
    boolean existsByTemplateName(String templateName);
}