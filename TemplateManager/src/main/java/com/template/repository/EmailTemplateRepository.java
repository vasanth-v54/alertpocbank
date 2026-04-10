package com.template.repository;

import com.template.entity.EmailTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EmailTemplateRepository extends JpaRepository<EmailTemplate, Long> {

    java.util.Optional<EmailTemplate> findByTemplateName(String templateName);

    @Modifying
    @Query("UPDATE EmailTemplate e SET e.isActive = false, e.dateUpdated = CURRENT_TIMESTAMP " +
            "WHERE e.templateName = :templateName AND e.isActive = true")
    int deactivateTemplate(@Param("templateName") String templateName);

    @Modifying
    @Query("UPDATE EmailTemplate e SET e.isActive = true, e.dateUpdated = CURRENT_TIMESTAMP " +
            "WHERE e.templateName = :templateName AND e.isActive = false")
    int activateTemplate(@Param("templateName") String templateName);
}