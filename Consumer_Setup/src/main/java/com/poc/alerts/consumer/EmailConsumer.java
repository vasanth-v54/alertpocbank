package com.poc.alerts.consumer;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
public class EmailConsumer {

	private static final Logger log =
			LoggerFactory.getLogger(EmailConsumer.class);

	private static final Logger auditLog =
			LoggerFactory.getLogger("consumer_audit");

	private final AuditService auditService;
	private final RoutingService routingService;
	private final TemplateService templateService;

	private final ObjectMapper mapper = new ObjectMapper();

	public EmailConsumer(AuditService auditService,
						 RoutingService routingService,
						 TemplateService templateService) {

		this.auditService = auditService;
		this.routingService = routingService;
		this.templateService = templateService;
	}

	@KafkaListener(topics = "notifications.events",
			groupId = "notification-cg-email")
	public void consume(String payload,
						@Headers Map<String, Object> headers) {

		String eventId = "UNKNOWN_EVENT_ID"; // Default for DLT if extraction fails

		try {
			log.info("Message received for EMAIL consumer");

			// --- START EARLY FILTERING LOGIC ---
			// Extract messageType from headers first to decide if this consumer should process it
			String messageTypeHeader = null;
			Object alertTypeObj = headers.get("alert-type");
			if (alertTypeObj instanceof byte[]) {
				messageTypeHeader = new String((byte[]) alertTypeObj);
			} else if (alertTypeObj != null) {
				messageTypeHeader = alertTypeObj.toString();
			}

			if (messageTypeHeader == null || (!messageTypeHeader.equalsIgnoreCase("EMAIL") && !messageTypeHeader.equalsIgnoreCase("BOTH"))) {
				log.info("EMAIL | Skipping message with alert-type '{}' as it's not for this consumer.", messageTypeHeader);
				return; // Message not intended for EMAIL consumer, ignore it.
			}
			// --- END EARLY FILTERING LOGIC ---


			// Now proceed with validation and processing, as the message is for this consumer
			String headerValidationResult = HeaderValidator.validate(headers);
			if (!PocBankUtil.isNullOrEmpty(headerValidationResult)) {
				log.error("EMAIL | Invalid Header {}", headerValidationResult);
				writeDlt(headers, payload, "Header validation failed: " + headerValidationResult);
				return;
			}

			// Safe to extract eventId and messageType as header validation passed
			eventId = new String((byte[]) headers.get("event-id"));
			final String messageType = "EMAIL"; // Normalize for processing within this consumer

			log.info("EMAIL -> eventId: {}, messageType: {}", eventId, messageType);

			JsonNode root = mapper.readTree(payload);
			JsonNode custom = root.path("customFieldDetails");

			String[] fields = {
					"customer_id", "customer_name", "timestamp", "amount", "loan_account",
					"txn_ref", "days_overdue", "min_amount_due", "grace_date", "loan_ref",
					"beneficiary_name", "beneficiary_id"
			};

			for (String f : fields) {
				JsonNode node = custom.get(f);
				if (node == null || node.isNull() || node.asText().trim().isEmpty()) {
					writeDlt(headers, payload, "Payload validation failed: missing field -> " + f);
					return;
				}
			}

			auditLog.info("EMAIL | Consumed EventId={} Type={} Payload={}", eventId, messageType, payload);

			if (auditService.isAlreadyProcessed(eventId, messageType)) {
				auditLog.warn("EMAIL | Duplicate message detected for eventId={}", eventId);
				auditService.saveAudit(eventId, messageType, headers, payload, "DUPLICATE");
				writeDlt(headers, payload, "Duplicate message detected");
				return;
			}

			auditService.saveAudit(eventId, messageType, headers, payload, "CONSUMED");

			String alertType = PocBankUtil.getAlertType(payload);
			KeyRoutingConfig config = routingService.getRoutingConfig(messageType, alertType);
			log.info("EMAIL | Routing Config : {}", config);

			TemplateMst template = templateService.getTemplate(messageType, alertType);
			log.info("EMAIL | Template : {}", template);

		} catch (Exception e) {
			log.error("EMAIL | Error processing message", e);
			writeDlt(headers, payload, e.getMessage());
		}
	}

	private void writeDlt(Map<String, Object> headers, String payload, String error) {
		try {
			String eventId = "UNKNOWN_EVENT_ID";
			Object eventIdHeader = headers.get("event-id");
			if (eventIdHeader != null) {
				eventId = new String((byte[]) eventIdHeader);
			} else {
				try {
					JsonNode root = mapper.readTree(payload);
					JsonNode eventIdNode = root.get("eventId");
					if (eventIdNode != null) {
						eventId = eventIdNode.asText();
					}
				} catch (Exception e) {
					log.error("Failed to extract eventId from payload for DLT log", e);
				}
			}

			DltLog logObj = DltLoggerUtil.build(
					"ConsumerService",
					"400",
					HeaderValidator.extractHeaders(headers).toString(),
					eventId,
					error,
					payload
			);
			new DltLoggerService(new FileDltLoggingStrategy()).log(logObj);
		} catch (Exception ex) {
			log.error("Failed writing DLT log", ex);
		}
	}
}
