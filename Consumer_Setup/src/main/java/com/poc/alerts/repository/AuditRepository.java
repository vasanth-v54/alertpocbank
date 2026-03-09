package com.poc.alerts.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.poc.alerts.entity.ConsumerEntryAudit;

@Repository
public interface AuditRepository extends JpaRepository<ConsumerEntryAudit,Long> {

}