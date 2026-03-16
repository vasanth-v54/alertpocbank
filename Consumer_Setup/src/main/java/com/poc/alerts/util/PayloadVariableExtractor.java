package com.poc.alerts.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class PayloadVariableExtractor {

    private static final ObjectMapper mapper = new ObjectMapper();
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

            if (valueNode != null && !valueNode.isNull()) {
                templateData.put(variable, valueNode.asText());
            } else {
                templateData.put(variable, "");
            }
        }

        return templateData;
    }

    /**
     * Recursively search JSON tree for a field name
     */
    @SuppressWarnings("deprecation")
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
}