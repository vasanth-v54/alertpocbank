package com.poc.alerts.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.poc.alerts.model.SmsRequest;

@Service
public class SmsService {

    private static final Logger log =
            LoggerFactory.getLogger(SmsService.class);

    //@Value("${sms.gateway.url:https://postman-echo.com/post}")
    @Value("${sms.gateway.url:https://control.msg91.com/api/v5/flow/}") 
    private String smsUrl;

    @Value("${sms.gateway.api-key:TEST_KEY}")
    private String apiKey;

    @Value("${sms.gateway.sender-id:POCBNK}")
    private String senderId;

    public void sendSms(String mobileNumber, String message) {

        try {

            log.info("Preparing SMS request");

            SmsRequest request = new SmsRequest();
            request.setMobileNumber(mobileNumber);
            request.setMessage(message);

            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("API_KEY", apiKey);

            HttpEntity<SmsRequest> entity =
                    new HttpEntity<>(request, headers);

            ResponseEntity<String> response =
                    restTemplate.postForEntity(
                    		smsUrl,
                            entity,
                            String.class);

            log.info("SMS Response Status : {}", response.getStatusCode());
            log.info("SMS Response Body : {}", response.getBody());

        } catch (Exception e) {

            log.error("SMS sending failed", e);

        }
    }
}