package com.template.repository;

import com.template.entity.SmsTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SmsTemplateRepository extends JpaRepository<SmsTemplate, Long> {
    Optional<SmsTemplate> findByTemplateName(String templateName);

    @Modifying
    @Query("UPDATE SmsTemplate s SET s.isActive = false, s.dateUpdated = CURRENT_TIMESTAMP " +
            "WHERE s.templateName = :templateName AND s.isActive = true")
    int deactivateTemplate(@Param("templateName") String templateName);

    @Modifying
    @Query("UPDATE SmsTemplate s SET s.isActive = true, s.dateUpdated = CURRENT_TIMESTAMP " +
            "WHERE s.templateName = :templateName AND s.isActive = false")
    int activateTemplate(@Param("templateName") String templateName);
}
