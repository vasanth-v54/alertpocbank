package com.notification.consumer.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.notification.consumer.dto.NotificationRequest;
import com.notification.consumer.service.AppConfigService;

public class TemplateEngineUtil {

    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * Builds NotificationRequest:
     * - result → final message OR missing keys info
     * - keyRequest → index → resolved value
     */
   
    @Autowired
    private AppConfigService appConfigService;

    private Set<String> getMaskFields() {
        return appConfigService.getMaskFields();
    }
    
    private String maskValue(String fieldName, String value) {

        if (value == null || value.isEmpty()) return value;

        Set<String> maskFields = getMaskFields();

        boolean shouldMask = maskFields.stream()
                .anyMatch(f -> f.equalsIgnoreCase(fieldName));

        if (!shouldMask) return value;

        int length = value.length();

        if (length <= 4) return "****";

        return "*".repeat(length - 4) + value.substring(length - 4);
    }
    
    public NotificationRequest buildMessage(String payload,
                                                   Map<String, Object> indexedContent,
                                                   Map<String, Object> paramMapping,
                                                   String contentKey) {

        Map<String, String> keyRequestMap = new HashMap<>();
        List<String> missingKeys = new ArrayList<>();

        try {
            // STEP 1: Parse JSON
            JsonNode root = mapper.readTree(payload);

            // STEP 2: Get template
            String template = String.valueOf(indexedContent.get(contentKey));

            // STEP 3: Iterate mapping
            for (Map.Entry<String, Object> entry : paramMapping.entrySet()) {

                String index = entry.getKey();                        // {0}
                String fieldName = String.valueOf(entry.getValue());  // field name

                // STEP 4: Find value
                String value = findValue(root, fieldName);

                if (value == null || value.isEmpty()) {
                    missingKeys.add(fieldName);
                    value = "";
                } else {
                    // APPLY MASKING HERE
                    value = maskValue(fieldName, value);
                }

                // store in keyRequest map
                keyRequestMap.put(fieldName, value);

                // replace placeholder
                template = template.replace("{" + index + "}", value);
            }

            // STEP 5: Prepare response
            NotificationRequest response = new NotificationRequest();
            response.setKeyRequest(keyRequestMap);

            if (!missingKeys.isEmpty()) {
                response.setResult(String.join(", ", missingKeys) + " missing in payload");
            } else {
                response.setResult(template);
            }

            return response;

        } catch (Exception e) {
            throw new RuntimeException("Error building message", e);
        }
    }

    /**
     * Recursive JSON search
     */
    private static String findValue(JsonNode node, String targetKey) {

        if (node == null) return "";

        // Direct match
        if (node.has(targetKey)) {
            JsonNode valueNode = node.get(targetKey);
            if (valueNode != null && !valueNode.isNull()) {
                return valueNode.asText("");
            }
        }

        // Object traversal
        if (node.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();

            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();

                String result = findValue(entry.getValue(), targetKey);
                if (!result.isEmpty()) return result;
            }
        }

        // Array traversal
        if (node.isArray()) {
            for (JsonNode element : node) {
                String result = findValue(element, targetKey);
                if (!result.isEmpty()) return result;
            }
        }

        return "";
    }
}