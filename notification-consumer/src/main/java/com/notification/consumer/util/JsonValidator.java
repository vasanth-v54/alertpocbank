package com.notification.consumer.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.notification.consumer.dto.ValidationResult;

import java.util.*;

public class JsonValidator {

    private static final ObjectMapper mapper = new ObjectMapper();

 // Keys to ignore (case-insensitive) 
    private static final Set<String> IGNORE_KEYS = new HashSet<>(Arrays.asList("messagetype","MessageType"));

    public static ValidationResult validate(String configJson, String payloadJson) {
        try {
            JsonNode configNode = mapper.readTree(configJson);
            JsonNode payloadNode = mapper.readTree(payloadJson);

            return validateNode(configNode, payloadNode, "", payloadNode);

        } catch (Exception e) {
            return ValidationResult.fail("Invalid JSON format: " + e.getMessage());
        }
    }

    private static ValidationResult validateNode(JsonNode configNode,
                                                 JsonNode payloadNode,
                                                 String path,
                                                 JsonNode rootPayload) {

        Iterator<Map.Entry<String, JsonNode>> fields = configNode.fields();

        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            String key = entry.getKey();
            JsonNode expectedValue = entry.getValue();

            String currentPath = path.isEmpty() ? key : path + "." + key;

            // ✅ 1. Ignore keys
            if (IGNORE_KEYS.contains(key.toLowerCase())) {
                continue;
            }

            // ✅ 2. Conditional check for originatingSource
            if (key.equalsIgnoreCase("originatingSource")) {

                String alertType = getValueIgnoreCase(rootPayload, "alertType");

                if (alertType == null ||
                        !alertType.equalsIgnoreCase("FUND_TRANSFER_SUCCESSFUL")) {
                    // Skip validation
                    continue;
                }
            }

            // ✅ 3. Find key (case-insensitive)
            Map.Entry<String, JsonNode> actualEntry =
                    findNodeCaseInsensitive(payloadNode, key);

            if (actualEntry == null) {
                return ValidationResult.fail("Missing key: " + currentPath);
            }

            JsonNode actualValue = actualEntry.getValue();

            // ✅ 4. Value comparison (case-insensitive)
            if (expectedValue.isValueNode()) {

                if (!actualValue.asText().equalsIgnoreCase(expectedValue.asText())) {
                    return ValidationResult.fail(
                            "Value mismatch at " + currentPath +
                                    " | Expected: " + expectedValue.asText() +
                                    " | Actual: " + actualValue.asText()
                    );
                }

            } else if (expectedValue.isObject()) {

                ValidationResult result =
                        validateNode(expectedValue, actualValue, currentPath, rootPayload);

                if (!result.isSuccess()) return result;
            }
        }

        return ValidationResult.success();
    }

    /**
     * Recursive search with CASE-INSENSITIVE key matching
     */
    private static Map.Entry<String, JsonNode> findNodeCaseInsensitive(JsonNode node,
                                                                       String searchKey) {

        if (node.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();

            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();

                if (entry.getKey().equalsIgnoreCase(searchKey)) {
                    return entry;
                }

                Map.Entry<String, JsonNode> result =
                        findNodeCaseInsensitive(entry.getValue(), searchKey);

                if (result != null) return result;
            }
        }

        if (node.isArray()) {
            for (JsonNode element : node) {
                Map.Entry<String, JsonNode> result =
                        findNodeCaseInsensitive(element, searchKey);
                if (result != null) return result;
            }
        }

        return null;
    }

    /**
     * Get value by key (case-insensitive, deep search)
     */
    private static String getValueIgnoreCase(JsonNode node, String key) {
        Map.Entry<String, JsonNode> entry = findNodeCaseInsensitive(node, key);
        return entry != null ? entry.getValue().asText() : null;
    }
}