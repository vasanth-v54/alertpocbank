package com.consumer.repository;

import com.consumer.entity.EwbPayloadAuditMtb;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EwbPayloadAuditRepository extends JpaRepository<EwbPayloadAuditMtb, Long> {
}