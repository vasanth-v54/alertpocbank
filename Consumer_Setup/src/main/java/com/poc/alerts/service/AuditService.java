package com.poc.alerts.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.poc.alerts.entity.ConsumerEntryAudit;
import com.poc.alerts.repository.AuditRepository;
import com.poc.alerts.util.PayloadParser;

@Service
public class AuditService {

	private static final Logger auditLog = LoggerFactory.getLogger("CONSUMER_AUDIT_LOGGER");
	private static final Logger log = LoggerFactory.getLogger(AuditService.class);

	@Autowired
	private AuditRepository auditRepository;

	public void saveAudit(String topic, String payload) {

		try {
			ConsumerEntryAudit audit = PayloadParser.parse(topic, payload);

			auditLog.info("EVENT_CONSUMED | topic={} | businessKey={} | messageType={} | eventType={} | payload={}",
					audit.getTopic(), audit.getBusinessKey(), audit.getMessageType(), audit.getEventType(),
					audit.getFullPayload());
			auditRepository.save(audit);

		} catch (Exception ex) {

			log.error("Failed to process Kafka message", ex);

		}

	}

}