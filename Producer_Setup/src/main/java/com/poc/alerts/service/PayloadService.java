package com.poc.alerts.service;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.poc.alerts.entity.PayloadMst;
import com.poc.alerts.repository.PayloadRepository;

@Service
public class PayloadService {

    private final PayloadRepository payloadRepository;
    private final KafkaProducerService kafkaProducerService;

    private static final Logger log = LoggerFactory.getLogger(PayloadService.class);

    private final ObjectMapper mapper = new ObjectMapper();

    public PayloadService(PayloadRepository payloadRepository,
                          KafkaProducerService kafkaProducerService) {

        this.kafkaProducerService = kafkaProducerService;
        this.payloadRepository = payloadRepository;
    }

    @SuppressWarnings("unchecked")
	public void publishPayloads() throws Exception {

        log.info("========= PAYLOAD PUBLISH JOB STARTED ==========");

        List<PayloadMst> payloadList =
                payloadRepository.findByTopicStatus("PENDING");

        if (payloadList.isEmpty()) {

            log.info("No payload found in DB");
            log.info("Waiting for payload insertion...");
            return;
        }

        log.info("Total payload records fetched from DB : {}", payloadList.size());

        for (PayloadMst payload : payloadList) {

            try {
                log.info("-----------------------------------------------");
                log.info("Processing payload id : {}", payload.getId());
                log.info("Payload Type : {}", payload.getPayloadType());

                /*
                 * FULL JSON from DB
                 */
                String fullJson = payload.getPayload();

                JsonNode rootNode = mapper.readTree(fullJson);

                /*
                 * Extract MessageType (which will become alert-type header)
                 */
                JsonNode messageTypeNode = rootNode.path("payload")
                                                   .path("customFieldDetails")
                                                   .path("MessageType");
                String messageType = (messageTypeNode != null && !messageTypeNode.isNull()) ? messageTypeNode.asText() : null;

                /*
                 * Extract payload section
                 */
                JsonNode payloadNode = rootNode.path("payload");

                String payloadJson = mapper.writeValueAsString(payloadNode);

                /*
                 * Extract header fields (everything except payload)
                 */
                Map<String, Object> headerMap = mapper.convertValue(rootNode, Map.class);
                headerMap.remove("payload");

                /*
                 * Add the extracted messageType to the headers
                 */
                if (messageType != null) {
                    headerMap.put("alert-type", messageType);
                }

                String headerJson = mapper.writeValueAsString(headerMap);

                log.debug("Header JSON : {}", headerJson);
                log.debug("Payload JSON : {}", payloadJson);

                /*
                 * Send to Kafka
                 */
                kafkaProducerService.sendPayload(
                        payload.getId(),
                        payload.getPayloadType(),
                        headerJson,
                        payloadJson
                );

                payload.setTopicStatus("PUBLISHED");
                log.info("Payload id {} marked as PUBLISHED", payload.getId());

            } catch (Exception e) {
                log.error("Error processing payload id {}. Marking as FAILED.", payload.getId(), e);
                payload.setTopicStatus("FAILED");
            } finally {
                payloadRepository.save(payload);
            }
        }

        log.info("========= PAYLOAD PUBLISH JOB COMPLETED ==========");
    }
}
