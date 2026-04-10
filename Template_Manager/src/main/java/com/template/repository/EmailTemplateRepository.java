package com.template.repository;

import com.template.entity.EmailTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailTemplateRepository extends JpaRepository<EmailTemplate, Integer> {
    boolean existsByTemplateName(String templateName);
}