package com.templatemaster.repository;

import com.templatemaster.entity.TemplateMaster;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TemplateMasterRepository extends JpaRepository<TemplateMaster, Long> {
    boolean existsByTemplateNameAndMessageTypeAndIsActive(String templateName, String messageType, String isActive);
}