package com.notification.consumer.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.notification.consumer.dto.ValidationResult;

import java.util.*;

public class JsonValidator {

	private static final ObjectMapper mapper = new ObjectMapper();

	// Keys to ignore (case-insensitive)
	private static final Set<String> IGNORE_KEYS = new HashSet<>(Arrays.asList("messagetype", "MessageType"));

	public static ValidationResult validate(String configJson, String payloadJson) {
		try {
			JsonNode configNode = mapper.readTree(configJson);
			JsonNode payloadNode = mapper.readTree(payloadJson);

			return validateNode(configNode, payloadNode, "", payloadNode);

		} catch (Exception e) {
			return ValidationResult.fail("Invalid JSON format: " + e.getMessage());
		}
	}

	private static ValidationResult validateNode(JsonNode configNode, JsonNode payloadNode, String path,
			JsonNode rootPayload) {

		List<String> errors = new ArrayList<>();

		Iterator<Map.Entry<String, JsonNode>> fields = configNode.fields();

		while (fields.hasNext()) {
			Map.Entry<String, JsonNode> entry = fields.next();
			String key = entry.getKey();
			JsonNode expectedValue = entry.getValue();

			String currentPath = path.isEmpty() ? key : path + "." + key;

// ✅ Ignore keys
			if (IGNORE_KEYS.contains(key.toLowerCase())) {
				continue;
			}

// ✅ Conditional check for originatingSource
			if (key.equalsIgnoreCase("originatingSource")) {
				String alertType = getValueIgnoreCase(rootPayload, "alertType");

				if (alertType == null || !alertType.equalsIgnoreCase("FUND_TRANSFER_SUCCESSFUL")) {
					continue;
				}
			}

// ✅ Find key
			Map.Entry<String, JsonNode> actualEntry = findNodeCaseInsensitive(payloadNode, key);

			if (actualEntry == null) {
				errors.add("Missing key: " + currentPath);
				continue;
			}

			JsonNode actualValue = actualEntry.getValue();

// ✅ Value comparison
			if (expectedValue.isValueNode()) {

				if (!actualValue.asText().equalsIgnoreCase(expectedValue.asText())) {
					errors.add("Value mismatch at " + currentPath + " | Expected: " + expectedValue.asText()
							+ " | Actual: " + actualValue.asText());
				}

			} else if (expectedValue.isObject()) {

				ValidationResult result = validateNode(expectedValue, actualValue, currentPath, rootPayload);

				if (!result.isSuccess()) {
					errors.add(result.getMessage());
				}
			}
		}

// ✅ Final result
		if (errors.isEmpty()) {
			return ValidationResult.success();
		} else {
			return ValidationResult.fail(String.join(" | ", errors));
		}
	}

	/**
	 * Recursive search with CASE-INSENSITIVE key matching
	 */
	private static Map.Entry<String, JsonNode> findNodeCaseInsensitive(JsonNode node, String searchKey) {

		if (node.isObject()) {
			Iterator<Map.Entry<String, JsonNode>> fields = node.fields();

			while (fields.hasNext()) {
				Map.Entry<String, JsonNode> entry = fields.next();

				if (entry.getKey().equalsIgnoreCase(searchKey)) {
					return entry;
				}

				Map.Entry<String, JsonNode> result = findNodeCaseInsensitive(entry.getValue(), searchKey);

				if (result != null)
					return result;
			}
		}

		if (node.isArray()) {
			for (JsonNode element : node) {
				Map.Entry<String, JsonNode> result = findNodeCaseInsensitive(element, searchKey);
				if (result != null)
					return result;
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