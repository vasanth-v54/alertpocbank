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

@Service
public class NotificationService {
	private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
	private final AppConfigService configService;
	private final RoutingKeyConfigService routingService;

	public NotificationService(AppConfigService configService, RoutingKeyConfigService routingService) {
		super();
		this.configService = configService;
		this.routingService = routingService;
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
					RoutingKeyConfig config = routingService.findMatchingConfig(eventType, messageType);
					if (config != null) {
						log.info("config :: " + config);
					} else {
						log.info("Routing Key Configuration is Missing");
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

		System.out.println(result);
		return result;
	}

	public static boolean isNullOrEmpty(String value) {
		return value == null || value.isEmpty();
	}

}
