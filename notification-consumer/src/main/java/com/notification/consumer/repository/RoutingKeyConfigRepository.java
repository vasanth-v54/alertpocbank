package com.notification.consumer.repository;

import com.notification.consumer.entity.RoutingKeyConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoutingKeyConfigRepository extends JpaRepository<RoutingKeyConfig, Long> {

    List<RoutingKeyConfig> findByIsActiveTrue();
}