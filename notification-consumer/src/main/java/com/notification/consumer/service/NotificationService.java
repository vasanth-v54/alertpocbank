package com.notification.consumer.service;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {
	private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
	private final AppConfigService configService;
	
	public NotificationService(AppConfigService configService) {
		super();
		this.configService = configService;
	}

	public void process(String payload, Map<String, String> headers) {

		log.info("Payload: " + payload);
		log.info("Headers: " + headers);

	    String eventType = headers.get("event-type");
	    String alertType = headers.get("alert-type");
	    String MessageType = headers.get("MessageType");
	    log.info("**** Header Validation ****");
	    log.info("alertType : "+alertType );
	    log.info("eventType : "+eventType );
	    log.info("MessageType : "+MessageType );
	    
	}
}
