package com.notification.consumer.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.notification.consumer.entity.TemplateMaster;

@Repository
public interface TemplateMasterRepository extends JpaRepository<TemplateMaster, Long> {

    List<TemplateMaster> findByMessageTypeAndIsActive(
            String messageType,
            Integer isActive
    );
    
    List<TemplateMaster> findByIsActive(Integer isActive);
}