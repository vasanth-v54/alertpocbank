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

			// ================= STAGE 3 =================
			vlog.stageStart(3, "CONSUMER CORE - PAYLOAD CONFIG CHECK", eventId);
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

			

			String allowedConsumers = configService.getValue("ALLOWED_CONSUMER");
			Set<String> allowedSet = Arrays.stream(allowedConsumers.split("\\|")).collect(Collectors.toSet());
			

			if (!allowedSet.contains(messageType)) {
				String error = "Message type is NOT allowed";
				dltService.logDlt(payload, headers, error);
				vlog.field("MESSAGE_TYPE",      messageType);
				vlog.field("ERROR_MESSAGE",     error);
				vlog.field("EXECUTION_STOPPED", "Stage 3 — " + error);
				vlog.stageError(3, "CONSUMER CORE - PAYLOAD CONFIG CHECK", error, null, eventId);
				return;
			}

			boolean status = false;
			String precheck = null;
			// Prechecks
			log.info("precheck-message - "+messageType);
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
				log.info("precheck-message result - "+s1 + " "+ s2);
				if (s1 && s2) {
					status = true;
				} else {
					status = false;
					precheck = ("Both | Mobile Number or To is missing in payload");
				}
			}
			
			

			if (status && ((!isNullOrEmpty(alertType) && !isNullOrEmpty(eventType))
					|| (!isNullOrEmpty(eventType) && !isNullOrEmpty(originatingSource)))) {
				vlog.stageEnd(3, "CONSUMER CORE - PAYLOAD CONFIG CHECK", "SUCCESS", eventId);
				
				vlog.stageStart(4, "KEY ROUTING CONFIG TABLE CHECK", eventId);
				
				List<RoutingKeyConfig> matchedConfigs = routingService.findMatchingConfigs(payload);

				if (matchedConfigs.isEmpty()) {
					String error = "Routing Key Configuration missing";
					dltService.logDlt(payload, headers, error);
					vlog.field("ERROR_MESSAGE", error);
					vlog.stageError(4, "KEY ROUTING CONFIG TABLE CHECK", error, null, eventId);
					return;
				}
				log.info("Result Config: " + matchedConfigs);
				vlog.field("ALLOWED_CONSUMER_CHECK", "PASSED");
				vlog.field("FIELD_PRESENCE_CHECK",   "PASSED");
				vlog.field("MESSAGE_TYPE",           messageType);
				vlog.field("EVENT_TYPE",             eventType);
				vlog.field("ALERT_TYPE",             alertType);
				vlog.field("ROUTING_CONFIGS_FOUND",  matchedConfigs.toString());
				vlog.stageEnd(4, "KEY ROUTING CONFIG TABLE CHECK", "SUCCESS", eventId);
				
				List<RoutingKeyConfig> configs =
				        filterByMessageType(matchedConfigs, messageType);

				log.info("Filtered Configs: {}", configs);
				
				for (RoutingKeyConfig config : configs) {

					Map<String, TemplateMaster> templates = templateService
							.findTemplates(config.getTemplateIdentifiers(), payload);
					vlog.stageStart(5, "TEMPLATE MASTER LOOKUP", eventId);
					vlog.field("TemplateMaster",             templates.toString());
					
					if (templates == null || templates.isEmpty()) {
						String error = "Template Configuration missing";
						dltService.logDlt(payload, headers, error);
						vlog.field("ROUTING_CONFIG_ID", config.getId() != null ? config.getId().toString() : "N/A");
						vlog.field("ERROR_MESSAGE",     error);
						vlog.field("EXECUTION_STOPPED", "Stage 5 — " + error);
						vlog.stageError(5, "TEMPLATE MASTER LOOKUP", error, null, eventId);
						return;
					}
					vlog.stageEnd(5, "TEMPLATE MASTER LOOKUP", "SUCCESS", eventId);
					processTemplates(messageType, templates, payload, headers,eventId);
				}
				
			} else {
				String error = "Header validation failed: AlertType is null or empty" ;
				dltService.logDlt(payload, headers, error);
				vlog.field("ERROR_MESSAGE",     error);
				vlog.field("EXECUTION_STOPPED", "Stage 3 — " + error);
				vlog.stageError(3, "CONSUMER CORE - PAYLOAD CONFIG CHECK", error, null, eventId);
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
			Map<String, String> headers,String eventId) {
		vlog.stageStart(6, "DYNAMIC TEMPLATE PROCESSING LAYER", eventId);
		
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
			dltService.logDlt(payload, headers, "Invalid message type" + value);
		}

	}

	private NotificationRequest processSingle(TemplateMaster template, String type, String payload,
			Map<String, String> headers) {
		NotificationRequest request = new NotificationRequest();
		String eventId = headers != null ? headers.getOrDefault("event-id", "N/A") : "N/A";

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
					vlog.field("EXECUTION_STOPPED", "Stage 6 — " + error);
					vlog.stageError(6, "TEMPLATE LOOKUP", error, null, eventId);
					return null;
				}

				if (!isValidEmail(email)) {
					String error = "Invalid email format: " + email;
					dltService.logDlt(payload, headers, error);
					vlog.field("CHANNEL",           type);
					vlog.field("EMAIL_TO",          "INVALID");
					vlog.field("ERROR_MESSAGE",     error);
					vlog.field("EXECUTION_STOPPED", "Stage 6 — " + error);
					vlog.stageError(6, "TEMPLATE LOOKUP", error, null, eventId);
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
					vlog.field("EXECUTION_STOPPED", "Stage 6 — " + error);
					vlog.stageError(6, "TEMPLATE LOOKUP", error, null, eventId);
					return null;
				}

				if (!isValidMobile(mobile)) {
					String error = "Invalid mobileNumber format: " + mobile + " (must be 10 digits)";
					dltService.logDlt(payload, headers, error);
					vlog.field("CHANNEL",           type);
					vlog.field("MOBILE_NUMBER",          "INVALID");
					vlog.field("ERROR_MESSAGE",     error);
					vlog.field("EXECUTION_STOPPED", "Stage 6 — " + error);
					vlog.stageError(6, "TEMPLATE LOOKUP", error, null, eventId);
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
						vlog.field("EXECUTION_STOPPED", "Stage 6 — " + error);
						vlog.stageError(6, "TEMPLATE LOOKUP", error, null, eventId);
						return null;
					}

					//START
					/*if ("BOTH".equalsIgnoreCase(type)) {
						if (!isValidEmail(email) || !isValidMobile(mobile)) {
							String error = "Invalid email or mobileNumber for BOTH type";
							dltService.logDlt(payload, headers, error);
							return null;
						}
					}*/


					//END
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
					vlog.field("EXECUTION_STOPPED",     "Stage 6 — " + error);
					vlog.stageError(6, "TEMPLATE LOOKUP", error, null, eventId);
					return null;
				}

				resolvedParams.put(key, value);
			}

			request.setType(type);
			request.setTemplateParams(resolvedParams);
			vlog.stageEnd(6, "DYNAMIC TEMPLATE PROCESSING LAYER", "SUCCESS", eventId);
			log.info("Final Notification Request: " + request);
			vlog.stageStart(7, "FINAL RESULT", eventId);
			vlog.section("NOTIFICATION REQUEST — READY TO DISPATCH");
			vlog.field("FINAL_REQUEST_CAPTURED", request.toString());
			vlog.stageEnd(7, "FINAL NOTIFICATION RESULT", "SUCCESS", eventId);
			return request;

		} catch (Exception e) {
			String error = "Error processing template: " + e.getMessage();
			log.error("Exception in processSingle: ", error);
//			dltService.logDlt(payload, headers, error);
//			vlog.field("CHANNEL",           type);
//			vlog.field("ERROR_MESSAGE",     error);
//			vlog.field("EXECUTION_STOPPED", "Stage 5 — " + error);
//			vlog.stageError(5, "TEMPLATE LOOKUP", error, e, eventId);
			
			vlog.section("NOTIFICATION REQUEST — READY TO DISPATCH");
			vlog.field("FINAL_REQUEST_CAPTURED", request.toString());
			return request;
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

	private boolean isValidEmail(String email) {
		if (email == null) return false;

		return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.com$");
		//return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
	}

	private boolean isValidMobile(String mobile) {
		if (mobile == null) return false;

		// Only digits and exactly 10 digits
		return mobile.matches("^[0-9]{10}$");
	}
}