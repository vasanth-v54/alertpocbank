package com.poc.alerts.consumer;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.stereotype.Service;

import com.poc.alerts.util.HeaderValidator;
import com.poc.alerts.util.PocBankUtil;

@Service
public class EmailConsumer {

	private static final Logger log = LoggerFactory.getLogger(EmailConsumer.class);
	private static final Logger auditLog = LoggerFactory.getLogger("consumer_audit");

	@KafkaListener(topics = "notifications.events", groupId = "notification-cg-email")
	public void consume(String payload, @Headers Map<String, Object> headers) {

		try {

			log.info("****EMAIL CONSUMER****");

			String messageType = new String((byte[]) headers.get("alert-type"));
			String eventId = new String((byte[]) headers.get("event-id"));

			log.info("Email | eventId: {},messageType: {}", eventId, messageType);

			if (messageType.equalsIgnoreCase("EMAIL") || messageType.equals("BOTH")) {

				messageType = "EMAIL";

				// Audit log
				auditLog.info("EMAIL | Consumed EventId={} Type={} Payload={}", eventId, messageType, payload);

				// Validate headers
				String headerValidationResul = HeaderValidator.validate(headers);
				if (PocBankUtil.isNullOrEmpty(headerValidationResul)) {

					
					
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