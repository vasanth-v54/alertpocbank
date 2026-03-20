package com.notification.consumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
public class NotificationConsumerApplication {

	public static void main(String[] args) {
		SpringApplication.run(NotificationConsumerApplication.class, args);
	}

}
