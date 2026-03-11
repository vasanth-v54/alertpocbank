package com.poc.alerts.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.poc.alerts.entity.AuditMst;

public interface AuditRepository extends JpaRepository<AuditMst, Long> {
	boolean existsByEventIdAndMessageType(String eventId, String messageType);
}