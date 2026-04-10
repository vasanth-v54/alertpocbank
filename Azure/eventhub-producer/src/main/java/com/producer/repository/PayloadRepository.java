package com.producer.repository;

import com.producer.entity.PayloadMst;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PayloadRepository extends JpaRepository<PayloadMst, Long> {

    List<PayloadMst> findByTopicStatusAndIsActive(String topicStatus, Boolean isActive);
}