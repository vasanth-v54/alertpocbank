package com.notification.consumer.service;

import org.springframework.stereotype.Service;

import com.notification.consumer.entity.EmailTemplate;
import com.notification.consumer.repository.EmailTemplateRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailTemplateService implements CommonTemplateService {

    private final EmailTemplateRepository repository;

	@Override
    public String getTemplate(String templateName) {
        return repository.findByTemplateNameAndIsActiveTrue(templateName)
                .map(EmailTemplate::getTemplateBody)
                .orElseThrow(() -> new RuntimeException("Email Template not found: " + templateName));
    }
}
