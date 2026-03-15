package com.poc.alerts.util;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HeaderValidator {

	private static final Logger log = LoggerFactory.getLogger(HeaderValidator.class);

	public static String validate(Map<String, Object> headers) {

		String[] requiredHeaders = {
				"event-type",
				"event-id",
				"alert-type",
				"status",
				"source"
		};

		for (String header : requiredHeaders) {

			Object value = headers.get(header);
			String headerValue = null;

			if (value instanceof byte[]) {

				headerValue = new String((byte[]) value, StandardCharsets.UTF_8);

			} else if (value != null) {

				headerValue = value.toString();
			}

			log.info("Header [{}] value: {}", header, headerValue);

			/*
			 * -------------------------------------
			 * Validation: Null OR Empty OR Blank
			 * -------------------------------------
			 */
			if (headerValue == null || headerValue.trim().isEmpty()) {

				log.error("Header validation failed for header: {}", header);

				return "Missing or Empty header: " + header;
			}
		}

		return null;
	}

	public static Map<String, String> extractHeaders(Map<String, Object> headers) {

		Map<String, String> result = new HashMap<>();

		headers.forEach((key, value) -> {

			if (value instanceof byte[]) {

				result.put(key, new String((byte[]) value, StandardCharsets.UTF_8));

			} else if (value != null) {

				result.put(key, value.toString());
			}

		});

		return result;
	}
}