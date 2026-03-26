package com.notification.consumer.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Iterator;
import java.util.Map;

public class JsonSearchUtil {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static boolean search(String json, String searchKey) {
        try {
            JsonNode root = mapper.readTree(json);
            return searchNode(root, searchKey.toLowerCase());
        } catch (Exception e) {
            throw new RuntimeException("Invalid JSON", e);
        }
    }

    private static boolean searchNode(JsonNode node, String searchKey) {

        // 1. If object → check keys + recurse
        if (node.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();

            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();

                // ✅ Match key (case-insensitive)
                if (entry.getKey().equalsIgnoreCase(searchKey)) {

                    JsonNode value = entry.getValue();

                    // ✅ Check value exists and not empty
                    if (value != null &&
                        !value.isNull() &&
                        !value.asText().trim().isEmpty()) {
                        return true;
                    }
                }

                // Recurse
                if (searchNode(entry.getValue(), searchKey)) {
                    return true;
                }
            }
        }

        // 2. If array → iterate
        if (node.isArray()) {
            for (JsonNode element : node) {
                if (searchNode(element, searchKey)) {
                    return true;
                }
            }
        }

        return false;
    }
}