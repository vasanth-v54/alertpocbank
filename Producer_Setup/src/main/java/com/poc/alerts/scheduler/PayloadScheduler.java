package com.poc.alerts.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.poc.alerts.service.PayloadService;

@Component
public class PayloadScheduler {

    private static final Logger log = LoggerFactory.getLogger(PayloadScheduler.class);

    private final PayloadService payloadService;

    public PayloadScheduler(PayloadService payloadService) {
        this.payloadService = payloadService;
    }

    @Scheduled(fixedDelay = 10000)
    public void pollDatabase() throws JsonMappingException, JsonProcessingException {

        log.info("=================================================");
        log.info("Scheduler triggered : Checking DB for new payloads");

        payloadService.publishPayloads();

        log.info("Scheduler cycle completed");
        log.info("=================================================");

    }
}