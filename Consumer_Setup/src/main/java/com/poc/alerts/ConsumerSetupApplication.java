package com.poc.alerts;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
public class ConsumerSetupApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConsumerSetupApplication.class, args);
    }
}