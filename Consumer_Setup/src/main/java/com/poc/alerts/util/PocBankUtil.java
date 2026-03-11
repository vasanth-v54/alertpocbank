package com.poc.alerts.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class PocBankUtil {
	
	public static boolean isNullOrEmpty(String value) {
	    return value == null || value.trim().isEmpty();
	}
	
	public static String getAlertType(String json) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(json);

            return root
                    .path("customFieldDetails")
                    .path("alertType")
                    .asText();

        } catch (Exception e) {
            return null;
        }
    }

}
