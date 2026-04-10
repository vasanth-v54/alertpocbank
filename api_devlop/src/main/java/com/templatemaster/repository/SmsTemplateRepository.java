package com.templatemaster.repository;

import com.templatemaster.entity.SmsTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SmsTemplateRepository extends JpaRepository<SmsTemplate, Integer> {
    boolean existsByTemplateName(String templateName);
}