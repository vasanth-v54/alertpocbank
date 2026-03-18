package com.poc.alerts.util;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.poc.alerts.consumer.EmailConsumer;

public class HeaderValidator {

	private static final Logger log = LoggerFactory.getLogger(EmailConsumer.class);

	public static String validate(Map<String, Object> headers) {

		String[] requiredHeaders = { "event-type", "event-id", "alert-type", "status", "MessageType" };

		for (String header : requiredHeaders) {

			Object value = headers.get(header);
			String headerValue = null;

			if (value instanceof byte[]) {
				headerValue = new String((byte[]) value);
			} else if (value != null) {
				headerValue = value.toString();
			}
			log.info("headerValue: {}", headerValue);

			if (headerValue == null || headerValue.trim().isEmpty()) {
				return "Missing or Empty " + header + " header";
			}
		}

		return null;
	}

	public static Map<String, String> extractHeaders(Map<String, Object> headers) {

		Map<String, String> result = new HashMap<>();

		headers.forEach((key, value) -> {

			if (value instanceof byte[]) {
				result.put(key, new String((byte[]) value));
			} else if (value != null) {
				result.put(key, value.toString());
			}

		});

		return result;
	}

}