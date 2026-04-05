package com.notification.consumer.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Iterator;
import java.util.Map;

public class TemplateEngineUtil {

    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * MAIN METHOD
     * Builds final message from:
     * - payload JSON
     * - indexedContent (template)
     * - paramMapping (index → field)
     */
    public static String buildMessage(String payload,
                                      Map<String, Object> indexedContent,
                                      Map<String, Object> paramMapping,
                                      String contentKey) {

        try {
            // STEP 1: Parse JSON
            JsonNode root = mapper.readTree(payload);

            // STEP 2: Get template string
            String template = String.valueOf(indexedContent.get(contentKey));

            // STEP 3: Replace placeholders
            for (Map.Entry<String, Object> entry : paramMapping.entrySet()) {

                String index = entry.getKey();                     // {0}
                String fieldName = String.valueOf(entry.getValue()); // dynamic field

                // STEP 4: Find value dynamically anywhere in JSON
                String value = findValue(root, fieldName);

                // STEP 5: Replace placeholder
                template = template.replace("{" + index + "}", value);
            }

            return template;

        } catch (Exception e) {
            throw new RuntimeException("Error building message", e);
        }
    }

    /**
     * STEP 4 — Recursive JSON search
     * Finds value anywhere in payload
     */
    private static String findValue(JsonNode node, String targetKey) {

        if (node == null) return "";

        // Case 1: Direct match
        if (node.has(targetKey)) {
            JsonNode valueNode = node.get(targetKey);
            if (valueNode != null && !valueNode.isNull()) {
                return valueNode.asText("");
            }
        }

        // Case 2: Object → traverse fields
        if (node.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();

            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();

                String result = findValue(entry.getValue(), targetKey);
                if (!result.isEmpty()) return result;
            }
        }

        // Case 3: Array → traverse elements
        if (node.isArray()) {
            for (JsonNode element : node) {
                String result = findValue(element, targetKey);
                if (!result.isEmpty()) return result;
            }
        }

        return "";
    }
}