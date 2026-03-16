package com.poc.alerts.util;

import java.util.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class PayloadVariableExtractor {

    private static final ObjectMapper mapper = new ObjectMapper();

    // Configurable mask fields
    private static final Set<String> MASK_FIELDS = new HashSet<>(Arrays.asList(
            "DisbursementAccount",
            "ImmediateParentReference",
            "applicationcustomerid",
            "ReceiverAccount",
            "SenderAccount"
    ));

    /**
     * Extract values for template variables by searching the payload JSON
     */
    public static Map<String, Object> extractTemplateData(String payloadJson, String templateVariablesJson) throws Exception {

        JsonNode payloadNode = mapper.readTree(payloadJson);

        List<String> variables = mapper.readValue(
                templateVariablesJson,
                new TypeReference<List<String>>() {}
        );

        Map<String, Object> templateData = new HashMap<>();

        for (String variable : variables) {

            JsonNode valueNode = findValue(payloadNode, variable);

            String value = "";

            if (valueNode != null && !valueNode.isNull()) {
                value = valueNode.asText();
            }

            // Apply masking if required
            if (MASK_FIELDS.contains(variable)) {
                value = maskValue(value);
            }

            templateData.put(variable, value);
        }

        return templateData;
    }

    /**
     * Recursively search JSON tree for a field name
     */
    private static JsonNode findValue(JsonNode node, String fieldName) {

        if (node == null) {
            return null;
        }

        if (node.has(fieldName)) {
            return node.get(fieldName);
        }

        if (node.isObject()) {

            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();

            while (fields.hasNext()) {

                Map.Entry<String, JsonNode> entry = fields.next();

                JsonNode found = findValue(entry.getValue(), fieldName);

                if (found != null) {
                    return found;
                }
            }
        }

        if (node.isArray()) {

            for (JsonNode item : node) {

                JsonNode found = findValue(item, fieldName);

                if (found != null) {
                    return found;
                }
            }
        }

        return null;
    }

    /**
     * Mask value except last 4 characters
     */
    private static String maskValue(String value) {

        if (value == null || value.length() <= 4) {
            return value;
        }

        int maskLength = value.length() - 4;

        StringBuilder masked = new StringBuilder();

        for (int i = 0; i < maskLength; i++) {
            masked.append("*");
        }

        masked.append(value.substring(maskLength));

        return masked.toString();
    }
}