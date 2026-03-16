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
import com.poc.alerts.util.PayloadVariableExtractor;
import com.poc.alerts.util.PocBankUtil;

@Service
public class SmsConsumer {

	private static final Logger log = LoggerFactory.getLogger(SmsConsumer.class);
	private static final Logger auditLog = LoggerFactory.getLogger("consumer_audit");

	private final AuditService auditService;
	private final RoutingService routingService;
	private final TemplateService templateService;
	
	public SmsConsumer(AuditService auditService, RoutingService routingService,TemplateService templateService) {
		this.auditService = auditService;
		this.routingService = routingService;
		this.templateService = templateService;
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
				auditLog.info("SMS | Consumed EventId={} Type={} Payload={}", eventId, messageType, payload);

				// Validate headers
				log.info("*** Header Validation Starts ***");
				String headerValidationResult = HeaderValidator.validate(headers);
				if (PocBankUtil.isNullOrEmpty(headerValidationResult)) {
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
					log.info("KeyRoutingConfig config: {}",config);
			        log.info("SMS | Payload Actual data messageType: {}, alertType:{}",messageType,alertType);
			        log.info("SMS | Result from config data messageType: {}, alertType:{}",config.getMessageType(),config.getAlertType());

			        TemplateMst template =
			                templateService.getTemplate(messageType,alertType);

			        log.info("TemplateId : {}",template);
			        log.info("Template Variables : {},Payload Data : {}",template.getTemplateVariable(),payload);
			        
			        String templateVariables = template.getTemplateVariable();

			        Map<String,Object> templateData =
			                PayloadVariableExtractor.extractTemplateData(payload, templateVariables);

			        log.info("SMS | Template Data : {}", templateData);

			        

				} else {
					log.info("Invalid or Missing Header details {}", headerValidationResult);
					return;
				}
			}

		} catch (Exception e) {

			log.error("Error processing message", e);

		}

	}
}