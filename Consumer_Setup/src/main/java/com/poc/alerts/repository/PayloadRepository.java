package com.poc.alerts.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.poc.alerts.entity.PayloadMst;

public interface PayloadRepository extends JpaRepository<PayloadMst, Long> {

    @Modifying
    @Transactional
    @Query("UPDATE PayloadMst p SET p.topicStatus = :status WHERE p.id = :id")
    void updateTopicStatus(Long id, String status);
}