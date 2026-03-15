package com.poc.alerts.consumer;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.stereotype.Service;

import com.poc.alerts.entity.DltLog;
import com.poc.alerts.entity.KeyRoutingConfig;
import com.poc.alerts.entity.TemplateMst;
import com.poc.alerts.service.AuditService;
import com.poc.alerts.service.DltLoggerService;
import com.poc.alerts.service.RoutingService;
import com.poc.alerts.service.TemplateService;
import com.poc.alerts.strategy.impl.FileDltLoggingStrategy;
import com.poc.alerts.util.DltLoggerUtil;
import com.poc.alerts.util.HeaderValidator;
import com.poc.alerts.util.PocBankUtil;

@Service
public class SmsConsumer {

	private static final Logger log = LoggerFactory.getLogger(SmsConsumer.class);
	private static final Logger auditLog = LoggerFactory.getLogger("consumer_audit");

	private final AuditService auditService;
	private final RoutingService routingService;
	private final TemplateService templateService;

	public SmsConsumer(AuditService auditService,
					   RoutingService routingService,
					   TemplateService templateService) {

		this.auditService = auditService;
		this.routingService = routingService;
		this.templateService = templateService;
	}

	@KafkaListener(topics = "notifications.events", groupId = "notification-cg-sms")
	public void consume(String payload, @Headers Map<String, Object> headers) {

		try {

			log.info("Message received for SMS consumer");

			/*
			 * -------------------------------------------------
			 * STEP 1 : HEADER VALIDATION (FIRST)
			 * -------------------------------------------------
			 */

			String headerValidationResult = HeaderValidator.validate(headers);

			if (!PocBankUtil.isNullOrEmpty(headerValidationResult)) {

				log.error("SMS | Invalid or Missing Header {}", headerValidationResult);

				String eventId = headers.get("event-id") != null
						? new String((byte[]) headers.get("event-id"))
						: "UNKNOWN_EVENT_ID";

				DltLog logObj = DltLoggerUtil.build(
						"ConsumerService",
						"400",
						HeaderValidator.extractHeaders(headers).toString(),
						eventId,
						"Header validation failed: " + headerValidationResult,
						payload
				);

				new DltLoggerService(new FileDltLoggingStrategy()).log(logObj);
				return;
			}

			/*
			 * -------------------------------------------------
			 * STEP 2 : EXTRACT HEADERS
			 * -------------------------------------------------
			 */

			String messageType = new String((byte[]) headers.get("alert-type"));
			String eventId = new String((byte[]) headers.get("event-id"));

			log.info("SMS -> eventId: {}, messageType: {}", eventId, messageType);

			if (messageType.equalsIgnoreCase("SMS") || messageType.equalsIgnoreCase("BOTH")) {

				messageType = "SMS";

				auditLog.info("SMS | Consumed EventId={} Type={} Payload={}",
						eventId, messageType, payload);

				/*
				 * -------------------------------------------------
				 * STEP 3 : DUPLICATE CHECK
				 * -------------------------------------------------
				 */

				if (auditService.isAlreadyProcessed(eventId, messageType)) {

					auditLog.warn("SMS | Duplicate message detected for eventId={}", eventId);

					auditService.saveAudit(eventId, messageType, headers, payload, "DUPLICATE");

					DltLog logObj = DltLoggerUtil.build(
							"ConsumerService",
							"409",
							HeaderValidator.extractHeaders(headers).toString(),
							eventId,
							"Duplicate message detected",
							payload
					);

					new DltLoggerService(new FileDltLoggingStrategy()).log(logObj);
					return;
				}

				/*
				 * -------------------------------------------------
				 * STEP 4 : NORMAL PROCESSING
				 * -------------------------------------------------
				 */

				auditService.saveAudit(eventId, messageType, headers, payload, "CONSUMED");

				String alertType = PocBankUtil.getAlertType(payload);

				KeyRoutingConfig config =
						routingService.getRoutingConfig(messageType, alertType);

				log.info("SMS | Routing Config : {}", config);

				TemplateMst template =
						templateService.getTemplate(messageType, alertType);

				log.info("SMS | Template : {}", template);
			}

		} catch (Exception e) {

			log.error("SMS | Error processing message", e);

			try {

				String eventId = headers.get("event-id") != null
						? new String((byte[]) headers.get("event-id"))
						: "UNKNOWN_EVENT_ID";

				DltLog logObj = DltLoggerUtil.build(
						"ConsumerService",
						"500",
						HeaderValidator.extractHeaders(headers).toString(),
						eventId,
						e.getMessage(),
						payload
				);

				new DltLoggerService(new FileDltLoggingStrategy()).log(logObj);

			} catch (Exception dltEx) {

				log.error("Failed to write DLT log", dltEx);
			}
		}
	}
}