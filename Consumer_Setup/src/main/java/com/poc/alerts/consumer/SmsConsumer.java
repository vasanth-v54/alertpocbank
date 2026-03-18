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
public class SmsConsumer {

	private static final Logger log = LoggerFactory.getLogger(SmsConsumer.class);
	private static final Logger auditLog = LoggerFactory.getLogger("consumer_audit");

	@KafkaListener(topics = "notifications.events", groupId = "notification-cg-sms")
	public void consume(String payload, @Headers Map<String, Object> headers) {

		try {

			log.info("****SMS CONSUMER****");

			String messageType = new String((byte[]) headers.get("alert-type"));
			String eventId = new String((byte[]) headers.get("event-id"));

			log.info("SMS -> eventId: {},messageType: {}", eventId, messageType);

			if (messageType.equalsIgnoreCase("SMS") || messageType.equals("BOTH")) {

				messageType = "SMS";

				// Audit log
				auditLog.info("SMS | Consumed EventId={} Type={} Payload={}", eventId, messageType, payload);

				String headerValidationResult = HeaderValidator.validate(headers);
				if (PocBankUtil.isNullOrEmpty(headerValidationResult)) {

				} else {
					log.info("SMS | Invalid or Missing Header details {}", headerValidationResult);
					return;
				}
			}

		} catch (Exception e) {

			log.error("Error processing message", e);

		}

	}
}