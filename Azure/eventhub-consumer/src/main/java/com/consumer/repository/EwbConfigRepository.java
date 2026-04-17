package com.consumer.repository;

import com.consumer.entity.EwbConfigMtb;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EwbConfigRepository extends JpaRepository<EwbConfigMtb, Long> {

    // Get active config by key
    Optional<EwbConfigMtb> findByKeyAndIsActive(String key, Boolean isActive);

}