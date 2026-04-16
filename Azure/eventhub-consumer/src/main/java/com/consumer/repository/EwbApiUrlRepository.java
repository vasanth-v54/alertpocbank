package com.consumer.repository;

import com.consumer.entity.EwbApiUrlMtb;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EwbApiUrlRepository extends JpaRepository<EwbApiUrlMtb, Long> {
    EwbApiUrlMtb findByApiId(String apiId);
}