package com.template.repository;

import com.template.entity.TemplateMaster;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TemplateMasterRepository extends JpaRepository<TemplateMaster, Long> {
    boolean existsByTemplateNameAndMessageTypeAndIsActive(String templateName, String messageType, String isActive);

    TemplateMaster findTopByTemplateNameAndIsActiveOrderByVersionDesc(
            String templateName,
            String isActive
    );

    List<TemplateMaster> findByTemplateNameAndIsActive(String templateName, String isActive);

    List<TemplateMaster> findByIsActive(String isActive);

}