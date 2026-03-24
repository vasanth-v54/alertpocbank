package com.notification.consumer.service;

import java.util.Arrays;
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

@Service
public class NotificationService {
	private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
	private final AppConfigService configService;
	private final RoutingKeyConfigService routingService;
	private final TemplateService templateService;
	private final ObjectMapper objectMapper = new ObjectMapper();

	public NotificationService(AppConfigService configService, RoutingKeyConfigService routingService,
			TemplateService templateService) {
		super();
		this.configService = configService;
		this.routingService = routingService;
		this.templateService = templateService;
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
						Map<String, TemplateMaster> templates = templateService.findTemplates(configEventType,
								configAlertType);

						if (!templates.isEmpty() && null != templates) {
							log.info("Template master :: " + templates);
							processTemplates(messageType, templates,payload);

						} else {
							log.info("Template Configuration was Missing or Not Available ");
						}

					} else {
						log.info("Routing Key Configuration was Missing or Not Available");
					}

				}

			} else {
				log.info("Message type is NOT allowed");
			}

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

	private void processSingle(TemplateMaster template, String type, String payload) {

		if (template == null) {
			log.info(type + " template not found");
			return;
		}

		try {
			ObjectMapper mapper = new ObjectMapper();
			JsonNode json = mapper.readTree(template.getTemplateParameters());

			log.info("Template Parameters " + json.toString());

			if ("SMS".equalsIgnoreCase(type)) {

				// 👉 call SMS service here

			} else if ("EMAIL".equalsIgnoreCase(type)) {


				// 👉 call Email service here
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
