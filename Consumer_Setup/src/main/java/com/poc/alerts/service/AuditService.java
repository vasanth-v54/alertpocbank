package com.poc.alerts.service;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.poc.alerts.entity.AuditMst;
import com.poc.alerts.repository.AuditRepository;
import com.poc.alerts.util.HeaderValidator;

@Service
public class AuditService {

	private final AuditRepository auditRepository;
	private final ObjectMapper mapper = new ObjectMapper();

	public AuditService(AuditRepository auditRepository) {
		this.auditRepository = auditRepository;
	}

	public void saveAudit(String eventId, String messageType, Map<String, Object> headers, String payload,
			String status) throws Exception {

		AuditMst audit = new AuditMst();

		audit.setEventId(eventId);
		audit.setMessageType(messageType);
		Map<String, String> filteredHeaders = HeaderValidator.extractHeaders(headers);
		audit.setHeaders(mapper.writeValueAsString(filteredHeaders));

		audit.setPayload(payload);
		audit.setStatus(status);
		audit.setCreatedOn(LocalDateTime.now());
		audit.setCreatedBy("SYSTEM");

		auditRepository.save(audit);
	}
	
	public boolean isAlreadyProcessed(String eventId, String messageType) {

	    return auditRepository.existsByEventIdAndMessageType(eventId, messageType);
	}
}