package com.template.repository;

import com.template.entity.TemplateMaster;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TemplateMasterRepository extends JpaRepository<TemplateMaster, Long> {
    boolean existsByTemplateNameAndMessageTypeAndIsActive(String templateName, String messageType, String isActive);

    TemplateMaster findTopByTemplateNameAndIsActiveOrderByVersionDesc(
            String templateName,
            String isActive
    );

    List<TemplateMaster> findByTemplateNameAndIsActive(String templateName, String isActive);

    List<TemplateMaster> findByIsActive(String isActive);

    List<TemplateMaster> findByTemplateNameStartingWithAndMessageType(String templateName, String messageType);

    Optional<TemplateMaster> findByTemplateNameAndVersionAndIsActive(
            String templateName,
            String version,
            String isActive
    );
    Optional<TemplateMaster> findByTemplateNameAndVersion(String templateName, String version);
}