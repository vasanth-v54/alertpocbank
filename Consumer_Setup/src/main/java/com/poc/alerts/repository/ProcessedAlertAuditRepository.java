package com.poc.alerts.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.poc.alerts.entity.ProcessedAlertAudit;

public interface ProcessedAlertAuditRepository extends JpaRepository<ProcessedAlertAudit, Long> {
	boolean existsByEventIdAndMessageType(String eventId, String messageType);
}