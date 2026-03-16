package com.poc.alerts.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.poc.alerts.entity.PayloadMst;

public interface PayloadRepository extends JpaRepository<PayloadMst, Long> {

	List<PayloadMst> findByTopicStatus(String topicStatus);

	@Modifying
	@Transactional
	@Query("UPDATE PayloadMst p SET p.topicStatus = :status WHERE p.id = :id")
	void updateTopicStatus(Long id, String status);
}