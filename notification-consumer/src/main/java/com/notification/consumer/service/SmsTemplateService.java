package com.notification.consumer.service;

import org.springframework.stereotype.Service;

import com.notification.consumer.entity.SmsTemplate;
import com.notification.consumer.repository.SmsTemplateRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SmsTemplateService implements CommonTemplateService {

    private final SmsTemplateRepository repository;

    @Override
    public String getTemplate(String templateName) {
        return repository.findByTemplateNameAndIsActiveTrue(templateName)
                .map(SmsTemplate::getTemplateBody)
                .orElseThrow(() -> new RuntimeException("SMS Template not found: " + templateName));
    }
}
