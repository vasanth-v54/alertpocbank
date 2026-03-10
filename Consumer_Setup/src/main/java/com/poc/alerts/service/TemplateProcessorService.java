package com.poc.alerts.service;

import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.poc.alerts.entity.EventTemplateMapping;
import com.poc.alerts.entity.TemplateMst;
import com.poc.alerts.repository.EventTemplateMappingRepository;
import com.poc.alerts.repository.TemplateRepository;

@Service
public class TemplateProcessorService {

    private static final Logger log =
            LoggerFactory.getLogger(TemplateProcessorService.class);
    private static final Logger templateLog = LoggerFactory.getLogger("TEMPLATE_PROCESS_LOGGER");

    private final TemplateRepository templateRepository;
    private final EventTemplateMappingRepository eventTemplateMappingRepository;
    private final PayloadVariableExtractor payloadVariableExtractor;
    private final TemplateRenderer templateRenderer;

    public TemplateProcessorService(
            TemplateRepository templateRepository,
            EventTemplateMappingRepository eventTemplateMappingRepository,
            PayloadVariableExtractor payloadVariableExtractor,
            TemplateRenderer templateRenderer) {

        this.templateRepository = templateRepository;
        this.eventTemplateMappingRepository = eventTemplateMappingRepository;
        this.payloadVariableExtractor = payloadVariableExtractor;
        this.templateRenderer = templateRenderer;
    }

    public String processTemplate(String payload,
                                  String eventType,
                                  String alertType,
                                  String messageType) {

        log.info("=====================================================");
        log.info("TemplateProcessorService started");

        log.info("Finding template for AlertType={} MessageType={}",
                alertType, messageType);

        TemplateMst template =
                templateRepository
                        .findByAlertTypeAndMessageType(alertType, messageType);

        if(template == null){

            log.warn("No template found for AlertType={} MessageType={}",
                    alertType, messageType);

            return null;
        }

        String templateId = template.getTemplateId();

        log.info("TemplateId resolved : {}", templateId);

        EventTemplateMapping mapping =
                eventTemplateMappingRepository
                        .findByTemplateId(templateId);

        if(mapping == null){

            log.warn("No template mapping found for templateId={}",templateId);
            return null;
        }

        String templateParams =
                mapping.getTemplateParams();

        log.info("Template params fetched from DB : {}", templateParams);

        log.info("Extracting variables from payload");

        Map<String,String> values =
                payloadVariableExtractor.extractVariables(
                        payload,
                        templateParams
                );

        templateLog.info("AlertType={}, Final Result Before Masked : {}",alertType, values);
        

        Set<String> maskFields = Set.of(
                "DisbursementAccount",
                "ImmediateParentReference",
                "applicationcustomerid",
                "ReceiverAccount",
                "SenderAccount"
        );

        values.replaceAll((key, value) -> {

            if (maskFields.contains(key) && value != null && value.length() > 4) {
                String last4 = value.substring(value.length() - 4);
                return "*".repeat(value.length() - 4) + last4;
            }

            return value;
        });

        templateLog.info("AlertType={}, Final Result After Masked : {}",alertType, values);
        
//        String templateBody = template.getTemplate();
//
//        log.info("Template Body : {}", templateBody);
//
//        String finalMessage =
//                templateRenderer.render(templateBody,values);
//
//        log.info("Final message generated");
//
//        log.info("=====================================================");

        return "Template Procossing Completed";
    }
    
}