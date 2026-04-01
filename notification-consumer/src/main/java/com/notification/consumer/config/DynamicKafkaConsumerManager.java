package com.notification.consumer.config;

import com.notification.consumer.logger.VerticalLogger;
import com.notification.consumer.service.AppConfigService;
import com.notification.consumer.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

import com.notification.consumer.dlt.*;

@Configuration
@RequiredArgsConstructor
public class DynamicKafkaConsumerManager {

    private final AppConfigService configService;
    private final NotificationService notificationService;
    private final HeaderValidatorService headerValidatorService;
    private final DuplicateCheckService duplicateCheckService;
    private final VerticalLogger vlog;



	@PostConstruct
    public void startConsumer() {

        Integer consumerCount = configService.getInt("NO_OF_CONSUMER");

        if (consumerCount == null || consumerCount <= 0) {
            consumerCount = 1;
        }

        String groupId = "notification-cg";

        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory(groupId));

        //controls number of parallel consumers
        factory.setConcurrency(consumerCount);

        ConcurrentMessageListenerContainer<String, String> container =
                factory.createContainer("notifications.events");

        container.getContainerProperties().setGroupId(groupId);

        // Set listener
        container.getContainerProperties().setMessageListener(
                new MessageListener<String, String>() {

                    @Override
                    public void onMessage(ConsumerRecord<String, String> record) {

                        String payload = record.value();
                        Map<String, String> headers = extractHeaders(record);
                        String eventId = headers.getOrDefault("event-id", "N/A");

                        // ================= STAGE 1 =================
                        vlog.stageStart(1, "SEED SAMPLE PAYLOAD", eventId);
                        vlog.field("TOPIC",         record.topic());
                        vlog.field("PARTITION",     String.valueOf(record.partition()));
                        vlog.field("OFFSET",        String.valueOf(record.offset()));
                        vlog.field("EVENT_ID",      eventId);
                        vlog.field("EVENT_TYPE",    headers.getOrDefault("event-type", "N/A"));
                        vlog.field("MESSAGE_TYPE",  headers.getOrDefault("MessageType", "N/A"));
                        vlog.field("STATUS",        headers.getOrDefault("status", "N/A"));
                        vlog.section("RAW PAYLOAD RECIEVED");
                        vlog.stageEnd(1, "SEED SAMPLE PAYLOAD", "SUCCESS", eventId);

                        // Stage 2 wraps header validation + duplicate check
                        vlog.stageStart(2, "VALIDATIONS & DUPLICATE CHECK", eventId);

                        try {

                            // Step 1: Validate headers
                            vlog.section("STEP 2a — Header Validation");
                            headerValidatorService.validateHeaders(headers, payload);
                            vlog.field("HEADER_VALIDATION", "PASSED");
                            vlog.field("VALIDATED_HEADERS", "event-type, event-id, MessageType, status");

                            // Step 2: Duplicate check
                            vlog.section("STEP 2b — Duplicate Check");
                            vlog.field("EVENT_ID", eventId);
                            vlog.field("MESSAGE_TYPE", headers.getOrDefault("MessageType", "N/A"));
                            duplicateCheckService.checkDuplicate(eventId, payload, headers);
                            vlog.field("DUPLICATE_CHECK", "PASSED — first time seeing this event");
                            vlog.field("DB_AUDIT_SAVE", "SAVED — status=CONSUMED in consumer_entry_audit");
                            vlog.field("ERROR_MESSAGE", "NULL");

                            vlog.stageEnd(2, "VALIDATIONS & DUPLICATE CHECK", "SUCCESS", eventId);

                            // Step 3: Process if valid
                            notificationService.process(payload, headers);

                        } catch (Exception ex) {

                            // header validation or duplicate check threw — log rainy scenario
                            vlog.field("ERROR_MESSAGE", ex.getMessage());
                            vlog.stageError(2, "VALIDATIONS & DUPLICATE CHECK", ex.getMessage(), ex, eventId);

                            // Already logged to DLT
                            System.out.println("Message moved to DLT: " + ex.getMessage());
                        }
                    }
                }
        );

        container.setBeanName("dynamic-consumer-" + consumerCount);

        container.start();

        System.out.println("Started consumer with concurrency: " + consumerCount);
    }

    // 🔹 Extract headers
    private Map<String, String> extractHeaders(ConsumerRecord<String, String> record) {

        Map<String, String> headersMap = new HashMap<>();

        record.headers().forEach(header -> {
            if (header.value() != null) {
                headersMap.put(header.key(), new String(header.value()));
            }
        });

        return headersMap;
    }

    // Kafka consumer config
    private org.springframework.kafka.core.ConsumerFactory<String, String> consumerFactory(String groupId) {

        Map<String, Object> props = new HashMap<>();

        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        return new org.springframework.kafka.core.DefaultKafkaConsumerFactory<>(props);
    }
}