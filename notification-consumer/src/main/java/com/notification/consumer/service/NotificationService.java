package com.notification.consumer.service;

import java.util.*;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.*;
import com.notification.consumer.entity.*;
import com.notification.consumer.dlt.*;
import com.notification.consumer.dto.*;
import com.notification.consumer.logger.VerticalLogger;

@Service
public class NotificationService {

	private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

	private final AppConfigService configService;
	private final RoutingKeyConfigService routingService;
	private final TemplateService templateService;
	private final ObjectMapper objectMapper = new ObjectMapper();
	private final DltService dltService;
	private final VerticalLogger vlog;

	public NotificationService(AppConfigService configService,
							   RoutingKeyConfigService routingService,
							   TemplateService templateService,
							   DltService dltService,
							   VerticalLogger vlog) {
		this.configService = configService;
		this.routingService = routingService;
		this.templateService = templateService;
		this.dltService = dltService;
		this.vlog = vlog;
	}

	public void process(String payload, Map<String, String> headers) {

		String eventId = headers != null ? headers.getOrDefault("event-id", "N/A") : "N/A";

		try {
			ObjectMapper mapper = new ObjectMapper();

			// ================= STAGE 2 =================
			vlog.stageStart(2, "CONSUMER CORE", eventId);
			vlog.field("EVENT_ID", eventId);
			vlog.section("FULL PAYLOAD RECEIVED");
			vlog.payload(payload);

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

			String allowedConsumers = configService.getValue("ALLOWED_CONSUMER");
			Set<String> allowedSet = Arrays.stream(allowedConsumers.split("\\|")).collect(Collectors.toSet());

			// ================= STAGE 4 =================
			vlog.stageStart(4, "ROUTING CONFIG LOOKUP", eventId);

			if (!allowedSet.contains(messageType)) {
				String error = "Message type is NOT allowed";
				dltService.logDlt(payload, headers, error);
				vlog.stageError(4, "ROUTING CONFIG LOOKUP", error, null, eventId);
				return;
			}

			if ((!isNullOrEmpty(alertType) && !isNullOrEmpty(eventType))
					|| (!isNullOrEmpty(eventType) && !isNullOrEmpty(originatingSource))) {

				RoutingKeyConfig config = routingService.findMatchingConfig(eventType, alertType);

				if (config == null) {
					String error = "Routing Key Configuration missing";
					dltService.logDlt(payload, headers, error);
					vlog.stageError(4, "ROUTING CONFIG LOOKUP", error, null, eventId);
					return;
				}

				JsonNode json = objectMapper.readTree(config.getTemplateIdentifiers());
				String cfgEventType = json.path("eventType").asText();
				String cfgAlertType = json.path("alertType").asText();

				vlog.stageEnd(4, "ROUTING CONFIG LOOKUP", "SUCCESS", eventId);

				// ================= STAGE 5 =================
				vlog.stageStart(5, "TEMPLATE LOOKUP", eventId);

				Map<String, TemplateMaster> templates =
						templateService.findTemplates(cfgEventType, cfgAlertType);

				if (templates == null || templates.isEmpty()) {
					String error = "Template Configuration missing";
					dltService.logDlt(payload, headers, error);
					vlog.stageError(5, "TEMPLATE LOOKUP", error, null, eventId);
					return;
				}

				vlog.stageEnd(5, "TEMPLATE LOOKUP", "SUCCESS", eventId);

				// ================= STAGE 6 =================
				vlog.stageStart(6, "PROCESS TEMPLATE", eventId);

				processTemplates(messageType, templates, payload);

				vlog.stageEnd(6, "PROCESS TEMPLATE", "SUCCESS", eventId);

			} else {
				String error = "Invalid input";
				dltService.logDlt(payload, headers, error);
				vlog.stageError(4, "ROUTING CONFIG LOOKUP", error, null, eventId);
			}

		} catch (Exception e) {
			log.error("Exception", e);
			vlog.stageError(2, "CONSUMER CORE", e.getMessage(), null, eventId);
		}
	}

	public String extractCustomFieldDetails(String payload) throws Exception {
		JsonNode rootNode = objectMapper.readTree(payload);
		JsonNode customFieldDetails = rootNode.path("payload").path("customFieldDetails");
		return objectMapper.writeValueAsString(customFieldDetails);
	}

	public static boolean isNullOrEmpty(String value) {
		return value == null || value.isEmpty();
	}

	public void processTemplates(String value, Map<String, TemplateMaster> templateMap, String payload) {

		if (value == null || templateMap == null || templateMap.isEmpty()) {
			dltService.logDlt(payload, new HashMap<>(), "No templates available");
			return;
		}

		switch (value.toUpperCase()) {
			case "SMS":
				processSingle(templateMap.get("SMS"), "SMS", payload);
				break;
			case "EMAIL":
				processSingle(templateMap.get("EMAIL"), "EMAIL", payload);
				break;
			case "BOTH":
				processSingle(templateMap.get("SMS"), "SMS", payload);
				processSingle(templateMap.get("EMAIL"), "EMAIL", payload);
				break;
		}
	}

	private NotificationRequest processSingle(TemplateMaster template, String type, String payload) {

		if (template == null) {
			dltService.logDlt(payload, new HashMap<>(), "No templates available");
			return null;
		}

		try {
			JsonNode templateJson = objectMapper.readTree(template.getTemplateParameters());
			JsonNode payloadJson = objectMapper.readTree(payload);

			Map<String, String> resolvedParams = new HashMap<>();

			for (JsonNode param : templateJson.path("templateParams")) {
				String key = param.asText();
				String value = findValue(payloadJson, key);

				if (value == null) {
					dltService.logDlt(payload, new HashMap<>(), "Missing field " + key);
					return null;
				}

				resolvedParams.put(key, value);
			}

			NotificationRequest request = new NotificationRequest();
			request.setType(type);
			request.setTemplateParams(resolvedParams);

			return request;

		} catch (Exception e) {
			dltService.logDlt(payload, new HashMap<>(), "Error processing template");
			return null;
		}
	}

	private String findValue(JsonNode node, String key) {

		if (node == null) return null;

		if (node.has(key)) return node.get(key).asText();

		if (node.isObject()) {
			Iterator<Map.Entry<String, JsonNode>> it = node.fields();
			while (it.hasNext()) {
				String val = findValue(it.next().getValue(), key);
				if (val != null) return val;
			}
		}

		if (node.isArray()) {
			for (JsonNode child : node) {
				String val = findValue(child, key);
				if (val != null) return val;
			}
		}

		return null;
	}
}