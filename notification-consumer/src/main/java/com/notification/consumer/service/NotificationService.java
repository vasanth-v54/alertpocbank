package com.notification.consumer.service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.notification.consumer.entity.RoutingKeyConfig;
import com.notification.consumer.entity.TemplateMaster;
import com.notification.consumer.dlt.*;
import com.notification.consumer.dto.EmailRequest;
import com.notification.consumer.dto.NotificationRequest;
import com.notification.consumer.dto.SmsRequest;

@Service
public class NotificationService {
	private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
	private final AppConfigService configService;
	private final RoutingKeyConfigService routingService;
	private final TemplateService templateService;
	private final ObjectMapper objectMapper = new ObjectMapper();
	private final DltService dltService;

	public NotificationService(AppConfigService configService, RoutingKeyConfigService routingService,
			TemplateService templateService, DltService dltService) {
		super();
		this.configService = configService;
		this.routingService = routingService;
		this.templateService = templateService;
		this.dltService = dltService;
	}

	public void process(String payload, Map<String, String> headers) {

		try {
			ObjectMapper mapper = new ObjectMapper();
			log.info("Payload: " + payload);
			log.info("Headers: " + headers);

			JsonNode root = mapper.readTree(payload);
			String eventType = root.path("eventType").asText(null);

			String customFieldDetails = extractCustomFieldDetails(payload);
			log.info("customFieldDetails: " + customFieldDetails);
			JsonNode customroot = mapper.readTree(customFieldDetails);

			String messageType = customroot.path("MessageType").asText(null);
			String originatingSource = customroot.path("originatingsource").asText(null);
			String alertType = customroot.path("alertType").asText(null);

			log.info("alertType :: " + alertType);
			log.info("eventType :: " + eventType);
			log.info("MessageType : " + messageType);
			log.info("originatingSource : " + originatingSource);

			String allowedConsumers = configService.getValue("ALLOWED_CONSUMER");
			Set<String> allowedSet = Arrays.stream(allowedConsumers.split("\\|")).collect(Collectors.toSet());

			//start
			if (allowedSet.contains(messageType)) {
				log.info("Message type is allowed");

				if ((!isNullOrEmpty(alertType) && !isNullOrEmpty(eventType))
						|| (!isNullOrEmpty(eventType) && !isNullOrEmpty(originatingSource))) {

					RoutingKeyConfig config = routingService.findMatchingConfig(eventType, alertType);

					if (config != null) {

						log.info("config :: " + config);

						JsonNode json = objectMapper.readTree(config.getTemplateIdentifiers());

						String configEventType = json.path("eventType").asText();
						String configAlertType = json.path("alertType").asText();

						Map<String, TemplateMaster> templates =
								templateService.findTemplates(configEventType, configAlertType);

						if (templates != null && !templates.isEmpty()) {

							log.info("Template master :: " + templates);
							processTemplates(messageType, templates, payload);

						} else {

							String errorMessage = "Template Configuration missing";

							dltService.logDlt(payload, headers, errorMessage);
							return;
						}

					} else {

						String errorMessage = "Routing Key Configuration missing";

						dltService.logDlt(payload, headers, errorMessage);
						return;
					}

				} else {
					String errorMessage =
							"Invalid input: eventType must be present and either alertType or originatingSource must be present";

					log.info(errorMessage);

					dltService.logDlt(payload, headers, errorMessage);

					return;
				}

			} else {

				String errorMessage = "Message type is NOT allowed";

				log.info(errorMessage);

				dltService.logDlt(payload, headers, errorMessage);

				return;
			}

			//end

			/*else {
				log.info("Message type is NOT allowed");
			}*/

		} catch (Exception e) {
			log.info("Exception :: " + e);
		}

	}

	public String extractCustomFieldDetails(String payload) throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		JsonNode rootNode = mapper.readTree(payload);

		JsonNode customFieldDetails = rootNode.path("payload").path("customFieldDetails");

		// print as JSON
		String result = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(customFieldDetails);

		return result;
	}

	public static boolean isNullOrEmpty(String value) {
		return value == null || value.isEmpty();
	}

	public void processTemplates(String value, Map<String, TemplateMaster> templateMap, String payload) {

		if (value == null || templateMap == null || templateMap.isEmpty()) {
			log.info("No templates available");
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

		default:
			log.info("Invalid message type: " + value);
		}
	}

	private NotificationRequest processSingle(
	        TemplateMaster template,
	        String type,
	        String payload) {

	    if (template == null) {
	        log.info(type + " template not found");
	        return null;
	    }

	    try {
	        ObjectMapper mapper = new ObjectMapper();

	        JsonNode templateJson = mapper.readTree(template.getTemplateParameters());
	        JsonNode payloadJson = mapper.readTree(payload);

	        log.info("Template Parameters {}", templateJson.toString());

	        // 🔥 Extract templateParams
	        JsonNode paramsArray = templateJson.path("templateParams");

	        Map<String, String> resolvedParams = new HashMap<>();

	        if (paramsArray.isArray()) {

	            for (JsonNode param : paramsArray) {

	                String key = param.asText();
	                String value = findValue(payloadJson, key);

	                if (value == null) {
	                	log.info("❌ Data corrupted: Missing field -> " + key);
	                	return null;
	                }

	                resolvedParams.put(key, value);
	            }
	        }

	        // 🔥 Final request object
	        NotificationRequest request = new NotificationRequest();
	        request.setType(type);
	        request.setTemplateParams(resolvedParams);

	        // ======================
	        // 🔹 SMS
	        // ======================
	        if ("SMS".equalsIgnoreCase(type) || "BOTH".equalsIgnoreCase(type)) {

	            SmsRequest sms = new SmsRequest();

	            sms.setFrom(templateJson.path("from").asText());
	            sms.setMessage(templateJson.path("message").asText());
	            sms.setTemplate(templateJson.path("template").asText());
	            sms.setCallbackUrl(templateJson.path("callbackUrl").asText());
	            sms.setReferenceId(templateJson.path("referenceId").asText());
	            sms.setNotificationType(templateJson.path("notificationType").asText());

	            String mobile = findValue(payloadJson, "mobileNumber");

	            if (mobile == null || mobile.isEmpty()) {
	            	log.error("❌ Missing mobileNumber in payload");
	            	return null;
	            }

	            sms.setMobileNumber(mobile);

	            request.setSms(sms);
	        }

	        // ======================
	        // 🔹 EMAIL
	        // ======================
	        if ("EMAIL".equalsIgnoreCase(type) || "BOTH".equalsIgnoreCase(type)) {

	            EmailRequest email = new EmailRequest();

	            email.setFrom(templateJson.path("from").asText());
	            email.setCc(templateJson.path("cc").asText());
	            email.setBcc(templateJson.path("bcc").asText());
	            email.setSubject(templateJson.path("subject").asText());
	            email.setBody(templateJson.path("body").asText());
	            email.setTemplate(templateJson.path("template").asText());

	            String to = findValue(payloadJson, "to");

	            if (to == null || to.isEmpty()) {
	            	log.error("❌ Missing 'to' in payload");
	            	return null;
	            }

	            email.setTo(to);

	            request.setEmail(email);
	        }

	        log.error("Final Request"+request);
	        return request;

	    } catch (Exception e) {
	        log.error("❌ Error processing template: {}", e.getMessage(), e);
	        return null;
	    }
	}

	
	private String findValue(JsonNode node, String targetKey) {

	    if (node == null) return null;

	    // Direct match
	    if (node.has(targetKey)) {
	        return node.get(targetKey).asText();
	    }

	    // Traverse objects
	    if (node.isObject()) {
	        for (Iterator<Map.Entry<String, JsonNode>> it = node.fields(); it.hasNext();) {
	            Map.Entry<String, JsonNode> entry = it.next();
	            String result = findValue(entry.getValue(), targetKey);
	            if (result != null) return result;
	        }
	    }

	    // Traverse arrays
	    if (node.isArray()) {
	        for (JsonNode child : node) {
	            String result = findValue(child, targetKey);
	            if (result != null) return result;
	        }
	    }

	    return null;
	}
}
