package com.producer.scheduler;

import com.producer.service.EventHubProducerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PayloadScheduler {

    private static final Logger log = LoggerFactory.getLogger(PayloadScheduler.class);

    private final EventHubProducerService producerService;

    public PayloadScheduler(EventHubProducerService producerService) {
        this.producerService = producerService;
    }

    @Scheduled(fixedDelay = 10000) // every 10 sec AFTER previous execution
    public void pollDatabase() {

        log.info("=================================================");
        log.info("Scheduler triggered : Checking DB for PENDING payloads");

        try {

            // 🔥 CALL YOUR PRODUCER LOGIC
            producerService.sendPendingEvents();

        } catch (Exception ex) {

            log.error("Error while publishing payloads from scheduler", ex);

        }

        log.info("Scheduler cycle completed");
        log.info("=================================================");
    }
}