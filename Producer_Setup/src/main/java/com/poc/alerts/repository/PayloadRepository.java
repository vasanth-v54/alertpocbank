package com.poc.alerts.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.poc.alerts.entity.PayloadMst;

public interface PayloadRepository extends JpaRepository<PayloadMst, Long> {

	List<PayloadMst> findByTopicStatus(String topicStatus);

}