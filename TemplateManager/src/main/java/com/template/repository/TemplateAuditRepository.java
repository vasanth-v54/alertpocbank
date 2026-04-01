package com.template.repository;

import com.template.entity.TemplateAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TemplateAuditRepository extends JpaRepository<TemplateAudit, String> {
    List<TemplateAudit> findByTemplateId(String templateId);
}