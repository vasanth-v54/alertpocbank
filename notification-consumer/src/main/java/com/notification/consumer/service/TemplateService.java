package com.notification.consumer.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.notification.consumer.entity.TemplateMaster;
import com.notification.consumer.repository.TemplateMasterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TemplateService {

    private final TemplateMasterRepository repository;
    private final ObjectMapper objectMapper = new ObjectMapper();

	public Map<String, TemplateMaster> findTemplates(String eventType, String alertType) {

        List<TemplateMaster> templates = repository.findByIsActiveTrue();

        Map<String, TemplateMaster> resultMap = new HashMap<>();

        for (TemplateMaster template : templates) {

            try {
                JsonNode json = objectMapper.readTree(template.getTemplateIdentifiersRef());

                String dbAlertType = json.path("alertType").asText();
                String dbEventType = json.path("eventType").asText();

                if (alertType != null && eventType != null
                        && alertType.equalsIgnoreCase(dbAlertType)
                        && eventType.equalsIgnoreCase(dbEventType)) {

                    // 🔥 KEY = message_type column
                    String messageType = template.getMessageType();

                    resultMap.put(messageType, template);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return resultMap;
    }
}
