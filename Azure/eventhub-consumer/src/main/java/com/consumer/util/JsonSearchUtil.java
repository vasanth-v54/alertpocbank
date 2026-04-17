package com.consumer.util;
import java.util.Iterator;

import com.fasterxml.jackson.databind.JsonNode;

public class JsonSearchUtil {

    public static String findFirstValue(JsonNode node, String key) {

        if (node == null) return null;

        // If object
        if (node.isObject()) {

            Iterator<String> fieldNames = node.fieldNames();

            while (fieldNames.hasNext()) {
                String fieldName = fieldNames.next();
                JsonNode childNode = node.get(fieldName);

                if (fieldName.equalsIgnoreCase(key)) {
                    return childNode.asText();
                }

                String result = findFirstValue(childNode, key);
                if (result != null) {
                    return result; // 🔥 stop early
                }
            }
        }

        // If array
        else if (node.isArray()) {
            for (JsonNode item : node) {
                String result = findFirstValue(item, key);
                if (result != null) {
                    return result;
                }
            }
        }

        return null;
    }
}