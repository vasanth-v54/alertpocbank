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

		// This will be populated from headers, but we declare it here for the catch block
		String eventId = "UNKNOWN_EVENT_ID";

		try {

			log.info("Message received for EMAIL consumer");

        /*
         STEP 1 : HEADER VALIDATION
         */

			String headerValidationResult =
					HeaderValidator.validate(headers);

			if (!PocBankUtil.isNullOrEmpty(headerValidationResult)) {

				log.error("EMAIL | Invalid Header {}", headerValidationResult);

				writeDlt(headers, payload,
						"Header validation failed: " + headerValidationResult);

				return;
			}

        /*
         STEP 2 : EXTRACT HEADERS
         */

			String messageType =
					new String((byte[]) headers.get("alert-type"));

			// Now safe to extract, as HeaderValidator passed
			eventId = new String((byte[]) headers.get("event-id"));

			log.info("EMAIL -> eventId: {}, messageType: {}",
					eventId, messageType);

        /*
         STEP 3 : PAYLOAD VALIDATION
         */

			JsonNode root = mapper.readTree(payload);

        /*
         VALIDATE customFieldDetails
         */

			// CORRECTED PATH: The payload from Kafka IS the inner payload object.
			JsonNode custom = root.path("customFieldDetails");

			String[] fields = {

					"customer_id",
					"customer_name",
					"timestamp",
					"amount",
					"loan_account",
					"txn_ref",
					"days_overdue",
					"min_amount_due",
					"grace_date",
					"loan_ref",
					"beneficiary_name",
					"beneficiary_id"
			};

			for (String f : fields) {

				JsonNode node = custom.get(f);

				if (node == null ||
						node.isNull() ||
						node.asText().trim().isEmpty()) {

					writeDlt(headers, payload,
							"Payload validation failed: missing field -> " + f);

					return;
				}
			}

        /*
         STEP 4 : PROCESS BASED ON MESSAGE TYPE
         */

			if (messageType.equalsIgnoreCase("EMAIL") ||
					messageType.equalsIgnoreCase("BOTH")) {

				messageType = "EMAIL";

				auditLog.info(
						"EMAIL | Consumed EventId={} Type={} Payload={}",
						eventId, messageType, payload);

            /*
             DUPLICATE CHECK
             */

				if (auditService.isAlreadyProcessed(eventId, messageType)) {

					auditLog.warn(
							"EMAIL | Duplicate message detected for eventId={}",
							eventId);

					auditService.saveAudit(
							eventId,
							messageType,
							headers,
							payload,
							"DUPLICATE");

					writeDlt(headers, payload,
							"Duplicate message detected");

					return;
				}

            /*
             NORMAL PROCESSING
             */

				auditService.saveAudit(
						eventId,
						messageType,
						headers,
						payload,
						"CONSUMED");

				String alertType =
						PocBankUtil.getAlertType(payload);

				KeyRoutingConfig config =
						routingService.getRoutingConfig(
								messageType,
								alertType);

				log.info("EMAIL | Routing Config : {}", config);

				TemplateMst template =
						templateService.getTemplate(
								messageType,
								alertType);

				log.info("EMAIL | Template : {}", template);
			}

		} catch (Exception e) {

			log.error("EMAIL | Error processing message", e);

			writeDlt(headers, payload, e.getMessage());
		}
	}

/*
 DLT WRITER
 */

	private void writeDlt(Map<String, Object> headers,
						  String payload,
						  String error) {

		try {

			String eventId = "UNKNOWN_EVENT_ID";

			// First, try to get eventId from headers
			Object eventIdHeader = headers.get("event-id");
			if (eventIdHeader != null) {
                // This could be an empty string, which is fine.
				eventId = new String((byte[]) eventIdHeader);
			}

            // If the header was missing entirely, fall back to the original payload structure.
            // This is crucial for when header validation itself fails.
			if (eventIdHeader == null) {
				try {
					JsonNode root = mapper.readTree(payload);
                    // The original payload has the eventId at the root
					JsonNode eventIdNode = root.get("eventId");
					if (eventIdNode != null) { // Allow null, empty, or blank strings
						eventId = eventIdNode.asText();
					}
				} catch (Exception e) {
					log.error("Failed to extract eventId from payload for DLT log", e);
				}
			}


			DltLog logObj =
					DltLoggerUtil.build(
							"ConsumerService",
							"400",
							HeaderValidator.extractHeaders(headers).toString(),
							eventId,
							error,
							payload
					);

			new DltLoggerService(
					new FileDltLoggingStrategy()).log(logObj);

		} catch (Exception ex) {

			log.error("Failed writing DLT log", ex);
		}
	}


}
