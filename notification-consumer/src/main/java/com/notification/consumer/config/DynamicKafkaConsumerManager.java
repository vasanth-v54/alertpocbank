package com.notification.consumer.config;

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

@Configuration
@RequiredArgsConstructor
public class DynamicKafkaConsumerManager {

    private final AppConfigService configService;
    private final NotificationService notificationService;
    
    

    public DynamicKafkaConsumerManager(AppConfigService configService, NotificationService notificationService) {
		super();
		this.configService = configService;
		this.notificationService = notificationService;
	}

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

        // 🔥 KEY LINE → controls number of parallel consumers
        factory.setConcurrency(consumerCount);

        ConcurrentMessageListenerContainer<String, String> container =
                factory.createContainer("notifications.events");

        container.getContainerProperties().setGroupId(groupId);

        // 🔥 Set listener
        container.getContainerProperties().setMessageListener(
                new MessageListener<String, String>() {

                    @Override
                    public void onMessage(ConsumerRecord<String, String> record) {

                        String payload = record.value();

                        Map<String, String> headers = extractHeaders(record);

                        notificationService.process(payload, headers);
                    }
                }
        );

        container.setBeanName("dynamic-consumer-" + consumerCount);

        container.start();

        System.out.println("✅ Started consumer with concurrency: " + consumerCount);
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

    // 🔹 Kafka consumer config
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