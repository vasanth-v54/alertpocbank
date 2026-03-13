package com.poc.alerts.consumer;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.stereotype.Service;

import com.poc.alerts.entity.KeyRoutingConfig;
import com.poc.alerts.entity.TemplateMst;
import com.poc.alerts.service.AuditService;
import com.poc.alerts.service.RoutingService;
import com.poc.alerts.service.TemplateService;
import com.poc.alerts.util.HeaderValidator;
import com.poc.alerts.util.PocBankUtil;

@Service
public class EmailConsumer {

	private static final Logger log = LoggerFactory.getLogger(EmailConsumer.class);
	private static final Logger auditLog = LoggerFactory.getLogger("consumer_audit");

	private final AuditService auditService;
	private final RoutingService routingService;
	private final TemplateService templateService;
	
	public EmailConsumer(AuditService auditService, RoutingService routingService,TemplateService templateService) {
		this.auditService = auditService;
		this.routingService = routingService;
		this.templateService = templateService;
	}

	@KafkaListener(topics = "notifications.events", groupId = "notification-cg-email")
	public void consume(String payload, @Headers Map<String, Object> headers) {

		try {

			log.info("Message received for EMAIL consumer");

			String messageType = new String((byte[]) headers.get("alert-type"));
			String eventId = new String((byte[]) headers.get("event-id"));

			log.info("Email -> eventId: {},messageType: {}", eventId, messageType);

			if (messageType.equalsIgnoreCase("EMAIL") || messageType.equals("BOTH")) {

				messageType = "EMAIL";

				// Audit log
				auditLog.info("Consumed EventId={} Type={} Payload={}", eventId, messageType, payload);

				// Validate headers
				log.info("*** EMAIL | Header Validation Starts ***");
				String headerValidationResul = HeaderValidator.validate(headers);
				if (PocBankUtil.isNullOrEmpty(headerValidationResul)) {
					log.info("*** EMAIL | Header Validation Ends ***");

					// Step Duplicate check
					if (auditService.isAlreadyProcessed(eventId, messageType)) {

						auditLog.warn(" EMAIL | Duplicate message detected. Skipping processing for eventId={} messageType={}",
								eventId, messageType);
						// Save DB audit
						auditService.saveAudit(eventId, messageType, headers, payload, "DUPLICATE");
						return;
					}

					// Save DB audit
					auditService.saveAudit(eventId, messageType, headers, payload, "CONSUMED");
					
					// 🔹 Validate routing config
					String alertType=PocBankUtil.getAlertType(payload);
					KeyRoutingConfig config =
			                routingService.getRoutingConfig(messageType, alertType);

			        log.info("EMAIL | KeyRoutingConfig config: {}",config);
			        log.info("EMAIL | Payload Actual data messageType: {}, alertType:{}",messageType,alertType);
			        log.info("EMAIL | Result from config data messageType: {}, alertType:{}",config.getMessageType(),config.getAlertType());

			        TemplateMst template =
			                templateService.getTemplate(messageType,alertType);

			        log.info(" EMAIL | TemplateId : {}",template);
			        log.info(" EMAIL | Template Variables : {}",template.getTemplateVariable());

				} else {
					log.info(" EMAIL | Invalid or Missing Header details {}", headerValidationResul);
					return;
				}
			}
		} catch (Exception e) {

			log.error("Error processing message", e);

		}

	}
}