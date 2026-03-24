package com.notification.consumer.repository;

import com.notification.consumer.entity.TemplateMaster;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TemplateMasterRepository extends JpaRepository<TemplateMaster, Long> {

    List<TemplateMaster> findByIsActiveTrue();
}