package com.notification.consumer.service;

import lombok.extern.slf4j.Slf4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class NotificationService {

	private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
	
    public void handleEmail(String payload) {
        log.info("EMAIL processing -> {}", payload);
    }

    public void handleSms(String payload) {
        log.info("SMS processing -> {}", payload);
    }
}