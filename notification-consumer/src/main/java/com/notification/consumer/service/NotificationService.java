package com.notification.consumer.service;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.notification.consumer.dlt.DltService;
import com.notification.consumer.logger.VerticalLogger;
import com.notification.consumer.util.JsonSearchUtil;

@Service
public class NotificationService {

	private static final Logger log = LoggerFactory.getLogger(NotificationService.class);



	private final ObjectMapper objectMapper = new ObjectMapper();
	private final DltService dltService;
	private final VerticalLogger vlog;
	
	public NotificationService(DltService dltService, VerticalLogger vlog, Map<String, String> emptyHeaders) {
		super();
		this.dltService = dltService;
		this.vlog = vlog;
		this.emptyHeaders = emptyHeaders;
	}

	Map<String, String> emptyHeaders = new HashMap<>();

	public void process(String payload, Map<String, String> headers) {

		String eventId = headers != null ? headers.getOrDefault("event-id", "N/A") : "N/A";

		try {
			ObjectMapper mapper = new ObjectMapper();

			// ================= STAGE 2 =================
			vlog.stageStart(2, "CONSUMER CORE", eventId);
			vlog.field("EVENT_ID", eventId);
			vlog.section("PAYLOAD RECEIVED :: KEY PARAMETERS VALIDATED");

			JsonNode root = mapper.readTree(payload);
			String eventType = root.path("eventType").asText(null);

			String customFieldDetails = extractCustomFieldDetails(payload);
			JsonNode customroot = mapper.readTree(customFieldDetails);

			String messageType = customroot.path("MessageType").asText(null);
			String originatingSource = customroot.path("originatingsource").asText(null);
			String alertType = customroot.path("alertType").asText(null);

			vlog.field("EVENT_TYPE", eventType);
			vlog.field("ALERT_TYPE", alertType);
			vlog.field("MESSAGE_TYPE", messageType);

			vlog.stageEnd(2, "CONSUMER CORE", "SUCCESS", eventId);

			
			// ================= STAGE 4 =================
			vlog.stageStart(4, "ROUTING CONFIG LOOKUP", eventId);


			boolean status = false;
			String precheck = null;
			// Prechecks
			if (messageType.equalsIgnoreCase("SMS")) {
				status = JsonSearchUtil.search(payload, "mobileNumber");
				if (!status) {
					precheck = "SMS | 'mobileNumber' is missing";
				}
			} else if (messageType.equalsIgnoreCase("EMAIL")) {
				status = JsonSearchUtil.search(payload, "to");
				if (!status) {
					precheck = "EMAIL | 'to' is missing";
				}
			} else if (messageType.equalsIgnoreCase("Both")) {
				boolean s1 = JsonSearchUtil.search(payload, "mobileNumber");
				boolean s2 = JsonSearchUtil.search(payload, "to");
				if (s1 && s2) {
					status = true;
				} else {
					status = false;
					precheck = ("Both | Mobile Number or To is missing in payload");
				}
			}

			if (status && (!isNullOrEmpty(alertType) && !isNullOrEmpty(eventType))
					|| (!isNullOrEmpty(eventType) && !isNullOrEmpty(originatingSource))) {
				
				//Logic
			} else {
				String error = "Invalid input " + precheck;
				dltService.logDlt(payload, headers, error);
				vlog.field("ERROR_MESSAGE", error);
				vlog.field("EXECUTION_STOPPED", "Stage 4 — " + error);
				vlog.stageError(4, "ROUTING CONFIG LOOKUP", error, null, eventId);
				return;
			}

		} catch (Exception e) {
			log.error("Exception", e);
			vlog.stageError(2, "CONSUMER CORE", e.getMessage(), null, eventId);
			dltService.logDlt(payload, headers, e.getMessage());
		}
	}

	public String extractCustomFieldDetails(String payload) throws Exception {
		JsonNode rootNode = objectMapper.readTree(payload);
		JsonNode customFieldDetails = rootNode.path("payload").path("customFieldDetails");
		return objectMapper.writeValueAsString(customFieldDetails);
	}

	public static boolean isNullOrEmpty(String value) {
		return value == null || value.trim().isEmpty() || value.equalsIgnoreCase("null");
	}

	

	

	
}