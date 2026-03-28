package com.notification.consumer.service;

import java.util.*;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.notification.consumer.dlt.DltService;
import com.notification.consumer.dto.EmailRequest;
import com.notification.consumer.dto.NotificationRequest;
import com.notification.consumer.dto.SmsRequest;
import com.notification.consumer.entity.RoutingKeyConfig;
import com.notification.consumer.entity.TemplateMaster;
import com.notification.consumer.logger.VerticalLogger;
import com.notification.consumer.util.JsonSearchUtil;

@Service
public class NotificationService {

	private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

	private final AppConfigService configService;
	private final RoutingKeyConfigService routingService;
	private final TemplateService templateService;
	private final ObjectMapper objectMapper = new ObjectMapper();
	private final DltService dltService;
	private final VerticalLogger vlog;
	Map<String, String> emptyHeaders = new HashMap<>();

	public NotificationService(AppConfigService configService, RoutingKeyConfigService routingService,
			TemplateService templateService, DltService dltService, VerticalLogger vlog) {
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

			String allowedConsumers = configService.getValue("ALLOWED_CONSUMER");
			Set<String> allowedSet = Arrays.stream(allowedConsumers.split("\\|")).collect(Collectors.toSet());

			// ================= STAGE 4 =================
			vlog.stageStart(4, "ROUTING CONFIG LOOKUP", eventId);

			if (!allowedSet.contains(messageType)) {
				String error = "Message type is NOT allowed";
				dltService.logDlt(payload, headers, error);
				vlog.field("MESSAGE_TYPE",      messageType);
				vlog.field("ERROR_MESSAGE",     error);
				vlog.field("EXECUTION_STOPPED", "Stage 4 — " + error);
				vlog.stageError(4, "ROUTING CONFIG LOOKUP", error, null, eventId);
				return;
			}

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

				List<RoutingKeyConfig> matchedConfigs = routingService.findMatchingConfigs(payload);

				if (matchedConfigs.isEmpty()) {
					String error = "Routing Key Configuration missing";
					dltService.logDlt(payload, headers, error);
					vlog.field("ERROR_MESSAGE", error);
					vlog.stageError(4, "ROUTING CONFIG LOOKUP", error, null, eventId);
					return;
				}
				log.info("Result Config: " + matchedConfigs);
				vlog.field("ALLOWED_CONSUMER_CHECK", "PASSED");
				vlog.field("FIELD_PRESENCE_CHECK",   "PASSED");
				vlog.field("MESSAGE_TYPE",           messageType);
				vlog.field("EVENT_TYPE",             eventType);
				vlog.field("ALERT_TYPE",             alertType);
				vlog.field("ROUTING_CONFIGS_FOUND",  String.valueOf(matchedConfigs.size()));
				vlog.stageEnd(4, "ROUTING CONFIG LOOKUP", "SUCCESS", eventId);

				// ================= STAGE 5 =================
				vlog.stageStart(5, "TEMPLATE LOOKUP", eventId);
				vlog.field("MESSAGE_TYPE",           messageType);
				vlog.field("EVENT_TYPE",             eventType);
				vlog.field("ALERT_TYPE",             alertType);
				vlog.field("TEMPLATE_IDENTIFIERS",  matchedConfigs.stream()
						.findFirst()
						.map(RoutingKeyConfig::getTemplateIdentifiers)
						.orElse(null));
				
				List<RoutingKeyConfig> configs =
				        filterByMessageType(matchedConfigs, messageType);

				log.info("Filtered Configs: {}", configs);
				
				for (RoutingKeyConfig config : configs) {

					Map<String, TemplateMaster> templates = templateService
							.findTemplates(config.getTemplateIdentifiers(), payload);

					if (templates == null || templates.isEmpty()) {
						String error = "Template Configuration missing";
						dltService.logDlt(payload, headers, error);
						vlog.field("ROUTING_CONFIG_ID", config.getId() != null ? config.getId().toString() : "N/A");
						vlog.field("ERROR_MESSAGE",     error);
						vlog.field("EXECUTION_STOPPED", "Stage 5 — " + error);
						vlog.stageError(5, "TEMPLATE LOOKUP", error, null, eventId);
						return;
					}
					vlog.stageEnd(5, "TEMPLATE LOOKUP", "SUCCESS", eventId);
					log.info("Result Templates: " + templates);
					vlog.stageEnd(5, "TEMPLATE LOOKUP", "SUCCESS", eventId);
					processTemplates(messageType, templates, payload, headers);
				}
				// ================= STAGE 6 =================
				vlog.stageStart(6, "PROCESS TEMPLATE", eventId);
				vlog.field("MESSAGE_TYPE",           messageType);
				vlog.field("EVENT_TYPE",             eventType);
				vlog.field("ALERT_TYPE",             alertType);
				vlog.field("TEMPLATE",             templateService.findTemplates(matchedConfigs.get(0).getTemplateIdentifiers(), payload).toString());


				vlog.stageEnd(6, "PROCESS TEMPLATE", "SUCCESS", eventId);

			} else {
				String error = "Invalid input " + precheck;
				dltService.logDlt(payload, headers, error);
				vlog.field("ERROR_MESSAGE",     error);
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

	private static final ObjectMapper mapper = new ObjectMapper();

	public static List<RoutingKeyConfig> filterByMessageType(List<RoutingKeyConfig> configs, String inputType) {

		if (inputType == null)
			return configs;

		String type = inputType.toUpperCase();

		//BOTH → no filtering
		if ("BOTH".equals(type)) {
			return configs;
		}

		return configs.stream().filter(config -> {
			try {
				JsonNode node = mapper.readTree(config.getTemplateIdentifiers());

				String messageType = node.path("messageType").asText("");

				return messageType.equalsIgnoreCase(type);

			} catch (Exception e) {
				return false;
			}
		}).collect(Collectors.toList());
	}

	public String extractCustomFieldDetails(String payload) throws Exception {
		JsonNode rootNode = objectMapper.readTree(payload);
		JsonNode customFieldDetails = rootNode.path("payload").path("customFieldDetails");
		return objectMapper.writeValueAsString(customFieldDetails);
	}

	public static boolean isNullOrEmpty(String value) {
		return value == null || value.trim().isEmpty() || value.equalsIgnoreCase("null");
	}

	public void processTemplates(String value, Map<String, TemplateMaster> templateMap, String payload,
			Map<String, String> headers) {

		if (value == null || templateMap == null || templateMap.isEmpty()) {
			dltService.logDlt(payload, headers, "processTemplates No templates available");
			return;
		}

		log.info("Result Templates: " + templateMap + "value="+value);
		if (value.toUpperCase().equals("SMS")) {
			processSingle(templateMap.get("SMS"), "SMS", payload, headers);
		} else if (value.toUpperCase().equals("EMAIL")) {
			processSingle(templateMap.get("EMAIL"), "EMAIL", payload, headers);
		} else if (value.toUpperCase().equals("BOTH")) {
			vlog.field("CHANNEL", "BOTH");
			vlog.section("SMS + EMAIL — parallel execution");
			processSingle(templateMap.get("SMS"), "SMS", payload, headers);
			processSingle(templateMap.get("EMAIL"), "EMAIL", payload, headers);
		} else {
			log.info("Invalid Message Type");
			dltService.logDlt(payload, headers, "Invalid message type" + value)
		}

	}

	private NotificationRequest processSingle(TemplateMaster template, String type, String payload,
			Map<String, String> headers) {
		NotificationRequest request = new NotificationRequest();
		String eventId = headers != null ? headers.getOrDefault("event-id", "N/A") : "N/A";

		if (template == null) {
			String error = "No templates available — template object is null for " + type;
			dltService.logDlt(payload, headers, error);
			vlog.field("CHANNEL",           type);
			vlog.field("ERROR_MESSAGE",     error);
			vlog.field("EXECUTION_STOPPED", "Stage 5 — " + error);
			vlog.stageError(5, "TEMPLATE LOOKUP", error, null, eventId);
			return null;
		}
		try {
			JsonNode templateJson = objectMapper.readTree(template.getTemplateBody());
			JsonNode payloadJson = objectMapper.readTree(payload);

			String email = findValue(payloadJson, "to");
			String mobile = findValue(payloadJson, "mobileNumber");

			//Validation based on message type
			if ("EMAIL".equalsIgnoreCase(type) || "BOTH".equalsIgnoreCase(type)) {

				if (isNullOrEmpty(email)) {
					dltService.logDlt(payload, headers, "Missing email (to) for EMAIL type");
					String error = "Missing email (to) for EMAIL type";
					vlog.field("CHANNEL",           type);
					vlog.field("EMAIL_TO",          "MISSING");
					vlog.field("ERROR_MESSAGE",     error);
					vlog.field("EXECUTION_STOPPED", "Stage 5 — " + error);
					vlog.stageError(5, "TEMPLATE LOOKUP", error, null, eventId);
					return null;
				}

				if ("BOTH".equalsIgnoreCase(type)) {

					if (isNullOrEmpty(email) || isNullOrEmpty(mobile)) {
						dltService.logDlt(payload, headers, "Missing email or mobileNumber for BOTH type");
						return null;
					}
				}
				EmailRequest emailRequest = new EmailRequest();

				emailRequest.setFrom(templateJson.path("from").asText());
				emailRequest.setCc(templateJson.path("cc").asText());
				emailRequest.setBcc(templateJson.path("bcc").asText());
				emailRequest.setSubject(templateJson.path("subject").asText());
				emailRequest.setTemplate(template.getTemplateid());
				emailRequest.setTo(email);
				request.setEmail(emailRequest);
			}

			if ("SMS".equalsIgnoreCase(type) || "BOTH".equalsIgnoreCase(type)) {

				if (isNullOrEmpty(mobile)) {
					String error = "Missing mobileNumber for SMS";
					dltService.logDlt(payload, headers, error);
					vlog.field("CHANNEL",           type);
					vlog.field("MOBILE_NUMBER",     "MISSING");
					vlog.field("ERROR_MESSAGE",     error);
					vlog.field("EXECUTION_STOPPED", "Stage 5 — " + error);
					vlog.stageError(5, "TEMPLATE LOOKUP", error, null, eventId);
					return null;
				}

				if ("BOTH".equalsIgnoreCase(type)) {

					if (isNullOrEmpty(email) || isNullOrEmpty(mobile)) {
						String error = "Missing email or mobileNumber for BOTH type";
						dltService.logDlt(payload, headers, error);
						vlog.field("CHANNEL",           type);
						vlog.field("EMAIL_TO",          email  != null ? email  : "MISSING");
						vlog.field("MOBILE_NUMBER",     mobile != null ? mobile : "MISSING");
						vlog.field("ERROR_MESSAGE",     error);
						vlog.field("EXECUTION_STOPPED", "Stage 5 — " + error);
						vlog.stageError(5, "TEMPLATE LOOKUP", error, null, eventId);
						return null;
					}
				}
				SmsRequest sms = new SmsRequest();

				sms.setFrom(templateJson.path("from").asText());
				sms.setMessage(templateJson.path("message").asText());
				sms.setTemplate(template.getTemplateid());
				sms.setCallbackUrl(templateJson.path("callbackUrl").asText());
				sms.setReferenceId(templateJson.path("referenceId").asText());
				sms.setNotificationType(type);
				sms.setMobileNumber(mobile);
				request.setSms(sms);
			}

			Map<String, String> resolvedParams = new HashMap<>();

			for (JsonNode param : templateJson.path("templateParams")) {
				String key = param.asText();
				String value = findValue(payloadJson, key);

				if (isNullOrEmpty(value)) {
					String error = "Missing template param: " + key;
					dltService.logDlt(payload, headers, error);
					vlog.field("CHANNEL",               type);
					vlog.field("MISSING_TEMPLATE_PARAM", key);
					vlog.field("ERROR_MESSAGE",         error);
					vlog.field("EXECUTION_STOPPED",     "Stage 5 — " + error);
					vlog.stageError(5, "TEMPLATE LOOKUP", error, null, eventId);
					return null;
				}

				resolvedParams.put(key, value);
			}

			request.setType(type);
			request.setTemplateParams(resolvedParams);
			log.info("Final Notification Request: " + request);
			vlog.section("NOTIFICATION REQUEST — READY TO DISPATCH");
			vlog.field("FINAL_REQUEST_CAPTURED", request.toString());
			return request;

		} catch (Exception e) {
			String error = "Error processing template: " + e.getMessage();
			log.error("Exception in processSingle: ", e);
			dltService.logDlt(payload, headers, error);
			vlog.field("CHANNEL",           type);
			vlog.field("ERROR_MESSAGE",     error);
			vlog.field("EXECUTION_STOPPED", "Stage 5 — " + error);
			vlog.stageError(5, "TEMPLATE LOOKUP", error, e, eventId);
			return null;
		}
	}

	private static final Set<String> MASK_KEYS = new HashSet<>(Arrays.asList("disbursementaccount",
			"immediateparentreference", "applicationcustomerid", "receiveraccount", "senderaccount"));

	private String findValue(JsonNode node, String key) {

		if (node == null)
			return null;

		//Case-insensitive key match
		if (node.isObject()) {
			Iterator<Map.Entry<String, JsonNode>> fields = node.fields();

			while (fields.hasNext()) {
				Map.Entry<String, JsonNode> entry = fields.next();

				String currentKey = entry.getKey();
				JsonNode valueNode = entry.getValue();

				//Match key ignoring case
				if (currentKey.equalsIgnoreCase(key)) {

					String value = valueNode.isNull() ? null : valueNode.asText();

					//Apply masking if required
					if (value != null && MASK_KEYS.contains(currentKey.toLowerCase())) {
						return maskValue(value);
					}

					return value;
				}

				// Recurse
				String val = findValue(valueNode, key);
				if (val != null)
					return val;
			}
		}

		//Array handling
		if (node.isArray()) {
			for (JsonNode child : node) {
				String val = findValue(child, key);
				if (val != null)
					return val;
			}
		}

		return null;
	}

	private String maskValue(String value) {

		if (value.length() <= 4) {
			return value; // nothing to mask
		}

		int maskLength = value.length() - 4;
		String maskedPart = "*".repeat(maskLength);

		return maskedPart + value.substring(maskLength);
	}
}