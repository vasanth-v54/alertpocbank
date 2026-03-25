package com.poc.alerts.service;

import org.springframework.stereotype.Service;
import com.poc.alerts.repository.TemplateRepository;
import com.poc.alerts.entity.TemplateMst;

@Service
@RequiredArgsConstructor
public class TemplateService {

    private final TemplateRepository repository;

   /*// public TemplateService(TemplateRepository repository) {
        this.repository = repository;
    }*/

    public TemplateMst getTemplate(String messageType,String alertType){

        return repository
                .findByMessageTypeAndAlertTypeAndIsActive(messageType,alertType,true)
                .orElseThrow(() -> new RuntimeException("Template not found"));
    }
}