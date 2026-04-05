package com.template.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.template.dto.*;
import com.template.entity.TemplateMaster;
import com.template.repository.TemplateRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TemplateService {

    private final TemplateRepository repository;
    private final ObjectMapper objectMapper;

       
    // GET TEMPLATE BY ID
       
    public TemplateResponseDTO getTemplateByTemplateName(String templateName) {

        List<TemplateMaster> templates =
                repository.findByTemplateNameAndIsActive(templateName, "1");

        if (templates == null || templates.isEmpty()) {
            throw new RuntimeException("Template not found for templateName: " + templateName);
        }

        templates = templates.stream()
                .sorted((t1, t2) -> Integer.compare(
                        Integer.parseInt(t2.getVersion()),
                        Integer.parseInt(t1.getVersion())
                ))
                .toList();

        TemplateMaster latest = templates.get(0);
        return mapToResponse(List.of(latest));
        //return mapToResponse(templates);
    }
    
    // GET ALL TEMPLATES
    public List<TemplateResponseDTO> getAllTemplates() {

        List<TemplateMaster> allTemplates = repository.findByIsActiveTrue();

        if (allTemplates == null || allTemplates.isEmpty()) {
            throw new RuntimeException("No templates found");
        }

        // GROUP BY templateName
        Map<String, List<TemplateMaster>> groupedTemplates =
                allTemplates.stream()
                        .collect(Collectors.groupingBy(TemplateMaster::getTemplateName));

        List<TemplateResponseDTO> responseList = new ArrayList<>();

        for (Map.Entry<String, List<TemplateMaster>> entry : groupedTemplates.entrySet()) {
//            responseList.add(mapToResponse(entry.getValue()));
            TemplateMaster latest = entry.getValue().stream()
                    .max(Comparator.comparing(t -> Integer.parseInt(t.getVersion())))
                    .orElse(null);

            if (latest != null) {
                responseList.add(mapToResponse(List.of(latest)));
            }
        }

        return responseList;
    }

       
    //   COMMON MAPPING LOGIC (CORE)
    private TemplateResponseDTO mapToResponse(List<TemplateMaster> templates) {

        TemplateResponseDTO response = new TemplateResponseDTO();
        RoutingConfig routingConfig = new RoutingConfig();
        TemplateContent content = new TemplateContent();

        List<ParamDTO> emailParams = new ArrayList<>();
        List<ParamDTO> smsParams = new ArrayList<>();

        boolean hasSMS = false;
        boolean hasEmail = false;

        try {

            for (TemplateMaster entity : templates) {

                    System.out.println("DEBUG entity: " + entity.getTemplateName()
                            + " | messageType=" + entity.getmessageType()
                            + " | rawContent=" + entity.getRawContent()
                            + " | headers=" + entity.getHeaders()
                            + " | paramMapping=" + entity.getParamMapping());

                Map<String, Object> alertConfig = parseJson(entity.getAlertConfig());
                Map<String, Object> headers = parseJson(entity.getHeaders());
                Map<String, Object> rawContent = parseJson(entity.getRawContent());
                Map<String, Object> rawParamMap = parseJson(entity.getParamMapping());
                Map<String, Object> indexedContent = parseJson(entity.getIndexContent());

                Map<String, String> paramMap = new HashMap<>();
                if (rawParamMap != null) {
                    rawParamMap.forEach((k, v) -> paramMap.put(k, String.valueOf(v)));
                }

                // SET ROUTING CONFIG
                if (routingConfig.getTemplateName() == null) {
                    routingConfig.setTemplateName(entity.getTemplateName());
                    routingConfig.setAlertId((String) alertConfig.get("alertId"));
                    routingConfig.setAlertName((String) alertConfig.get("alertName"));
                    routingConfig.setDomain((String) alertConfig.get("domain"));
                    routingConfig.setEventType((String) alertConfig.get("eventType"));
                    routingConfig.setAlertType((String) alertConfig.get("alertType"));
                    routingConfig.setVersion(entity.getVersion());

                    routingConfig.setStatus(
                            "1".equals(entity.getIsActive()) ? "ACTIVE" : "INACTIVE"
                    );
                }

                String messageType = entity.getmessageType() != null
                        ? entity.getmessageType().trim()
                        : "";
                
                if ("EMAIL".equalsIgnoreCase(messageType)) {

                    hasEmail = true;

                    String emailContent = null;

                    if (rawContent != null) {
                        Object contentObj = rawContent.get("emailContent");

                        if (contentObj instanceof String) {
                            emailContent = (String) contentObj;
                        } else {
                            System.out.println("emailContent missing or wrong type. Keys: " + rawContent.keySet());
                        }
                    }

                    content.setEmailContent(emailContent);
                    if (indexedContent != null) {
                        content.setTemplateBody((String) indexedContent.get("emailContent"));
                    }

                    Map<String, Object> emailHeaders = null;

                    if (headers != null) {
                        Object headerObj = headers.get("email");

                        if (headerObj instanceof Map) {
                            emailHeaders = (Map<String, Object>) headerObj;
                        } else {
                            System.out.println("email headers missing or wrong type. Keys: " + headers.keySet());
                        }
                    }

                    content.setEmailHeaders(emailHeaders);

                    emailParams = mapParams(paramMap);
                }

                if ("SMS".equalsIgnoreCase(messageType)) {

                    hasSMS = true;

                    content.setSmsContent(
                            rawContent != null ? (String) rawContent.get("smsContent") : null
                    );

                    if (indexedContent != null) {
                        content.setTemplateBody((String) indexedContent.get("smsContent"));
                    }

                    content.setSmsHeaders(
                            headers != null ? (Map<String, Object>) headers.get("sms") : null
                    );

                    smsParams = mapParams(paramMap);
                }
            }

            // FINAL MESSAGE TYPE
            String finalMessageType;
            if (hasSMS && hasEmail) {
                finalMessageType = "BOTH";
            } else if (hasSMS) {
                finalMessageType = "SMS";
            } else {
                finalMessageType = "EMAIL";
            }

            routingConfig.setMessageType(finalMessageType);
            content.setVersion(routingConfig.getVersion());

            // FINAL RESPONSE
            response.setRoutingConfig(routingConfig);
            response.setTemplateContent(content);
            response.setEmailParams(emailParams);
            response.setSmsParams(smsParams);

            return response;

        } catch (Exception e) {
            throw new RuntimeException("Error mapping template data", e);
        }
    }

       
    private Map<String, Object> parseJson(String json) {
        try {
            if (json == null || json.isEmpty()) return null;
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            System.out.println("JSON parsing error::"+ e.getMessage());
            System.out.println(json);
            e.printStackTrace();
            return null;
        }
    }

       
    // PARAM MAPPING
       
    private List<ParamDTO> mapParams(Map<String, String> paramMap) {

        if (paramMap == null || paramMap.isEmpty()) {
            return new ArrayList<>();
        }

        return paramMap.entrySet()
                .stream()
                .map(entry -> {
                    try {
                        int seq = Integer.parseInt(entry.getKey().trim()); //   IMPORTANT FIX
                        return new ParamDTO(seq, entry.getValue());
                    } catch (Exception e) {
                        System.out.println("Invalid param key: " + entry.getKey());
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingInt(ParamDTO::getSeq))
                .toList();
    }
}