package com.poc.alerts.consumer;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.stereotype.Service;

import com.poc.alerts.entity.KeyRoutingConfig;
import com.poc.alerts.service.AuditService;
import com.poc.alerts.service.RoutingService;
import com.poc.alerts.util.HeaderValidator;
import com.poc.alerts.util.PocBankUtil;

@Service
public class SmsConsumer {

	private static final Logger log = LoggerFactory.getLogger(SmsConsumer.class);
	private static final Logger auditLog = LoggerFactory.getLogger("consumer_audit");

	private final AuditService auditService;
	private final RoutingService routingService;

	public SmsConsumer(AuditService auditService, RoutingService routingService) {
		this.auditService = auditService;
		this.routingService = routingService;
	}

	@KafkaListener(topics = "notifications.events", groupId = "notification-cg-sms")
	public void consume(String payload, @Headers Map<String, Object> headers) {

		try {

			log.info("Message received for SMS consumer");

			String messageType = new String((byte[]) headers.get("alert-type"));
			String eventId = new String((byte[]) headers.get("event-id"));

			log.info("SMS -> eventId: {},messageType: {}", eventId, messageType);

			if (messageType.equalsIgnoreCase("SMS") || messageType.equals("BOTH")) {

				messageType = "SMS";

				// Audit log
				auditLog.info("Consumed EventId={} Type={} Payload={}", eventId, messageType, payload);

				// Validate headers
				log.info("*** Header Validation Starts ***");
				String headerValidationResul = HeaderValidator.validate(headers);
				if (PocBankUtil.isNullOrEmpty(headerValidationResul)) {
					log.info("*** Header Validation Ends ***");

					// Step Duplicate check
					if (auditService.isAlreadyProcessed(eventId, messageType)) {

						auditLog.warn("Duplicate message detected. Skipping processing for eventId={} messageType={}",
								eventId, messageType);
						// Save DB audit
						auditService.saveAudit(eventId, messageType, headers, payload, "DUPLICATE");
						return;
					}

					// Save DB audit
					auditService.saveAudit(eventId, messageType, headers, payload, "CONSUMED");

					// 🔹 Validate routing config
					String alertType = PocBankUtil.getAlertType(payload);
					KeyRoutingConfig config = routingService.getRoutingConfig(messageType, alertType);

					log.info("KeyRoutingConfig config: {}", config);

				} else {
					log.info("Invalid or Missing Header details {}", headerValidationResul);
					return;
				}
			}

		} catch (Exception e) {

			log.error("Error processing message", e);

		}

	}
}