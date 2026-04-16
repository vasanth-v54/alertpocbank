package com.consumer.repository;

import com.consumer.entity.EwbApiReqRespMtb;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EwbApiReqRespRepository extends JpaRepository<EwbApiReqRespMtb, Long> {
}