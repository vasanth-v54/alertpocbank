package com.notification.consumer.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.notification.consumer.entity.SmsTemplate;

@Repository
public interface SmsTemplateRepository extends JpaRepository<SmsTemplate, Integer> {

    Optional<SmsTemplate> findByTemplateNameAndIsActiveTrue(String templateName);
}
