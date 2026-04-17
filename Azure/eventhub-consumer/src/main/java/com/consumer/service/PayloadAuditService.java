package com.consumer.service;

import com.consumer.entity.EwbPayloadAuditMtb;
import com.consumer.repository.EwbPayloadAuditRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class PayloadAuditService {

    private final EwbPayloadAuditRepository repository;

    public PayloadAuditService(EwbPayloadAuditRepository repository) {
        this.repository = repository;
    }

    public void saveAudit(String payload, Integer partition, Long offset, Long sequence,String Status) {

        EwbPayloadAuditMtb audit = new EwbPayloadAuditMtb();
        audit.setPayload(payload);
        audit.setPartitionId(partition);
        audit.setOffsetValue(offset);
        audit.setSequence(sequence != null ? String.valueOf(sequence) : null);
        audit.setStatus(Status);
        audit.setCreatedBy("CONSUMER_MS");
        audit.setCreatedOn(LocalDateTime.now());

        repository.save(audit);
    }
}