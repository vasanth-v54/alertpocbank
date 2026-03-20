package com.notification.consumer.consumer;

import com.notification.consumer.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationConsumer {

	private final NotificationService notificationService;

	private static final Logger log = LoggerFactory.getLogger(NotificationConsumer.class);

	public NotificationConsumer(NotificationService notificationService) {
		super();
		this.notificationService = notificationService;
	}

	// 🔵 SMS Consumer Group
	@KafkaListener(topics = "notifications.events", groupId = "notification-cg-sms", containerFactory = "kafkaListenerContainerFactory")
	public void consumeSms(@Payload String payload, @Headers Map<String, Object> headers) {

		process(payload, headers, "SMS");
	}

	// 🟢 EMAIL Consumer Group
	@KafkaListener(topics = "notifications.events", groupId = "notification-cg-email", containerFactory = "kafkaListenerContainerFactory")
	public void consumeEmail(@Payload String payload, @Headers Map<String, Object> headers) {

		process(payload, headers, "EMAIL");
	}

	private void process(String payload, Map<String, Object> headers, String consumerType) {

		Object messageTypeObj = headers.get("MessageType");

		String messageType = messageTypeObj != null ? new String((byte[]) messageTypeObj) : null;

		log.info("[{}] MessageType={}", consumerType, messageType);

		if ("EMAIL".equalsIgnoreCase(messageType) || "BOTH".equalsIgnoreCase(messageType)) {
			
			notificationService.handleEmail(payload);
		} else {
			log.warn("[{}] Unknown MessageType: {}", consumerType, messageType);
		}

		if ("SMS".equalsIgnoreCase(messageType) || "BOTH".equalsIgnoreCase(messageType)) {
			notificationService.handleSms(payload);
		} else {
			log.warn("[{}] Unknown MessageType: {}", consumerType, messageType);
		}
	}
}