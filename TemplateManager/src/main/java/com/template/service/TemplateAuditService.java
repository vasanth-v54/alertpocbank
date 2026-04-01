package com.template.service;

import com.template.entity.TemplateAudit;
import com.template.exception.ResourceNotFoundException;
import com.template.repository.TemplateAuditRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class TemplateAuditService {

    private final TemplateAuditRepository repository;

    public TemplateAuditService(TemplateAuditRepository repository) {
        this.repository = repository;
    }

    public List<TemplateAudit> getAuditsByTemplateId(Long templateId) {
        List<TemplateAudit> audits = repository.findByTemplateId(templateId);
        if (audits.isEmpty()) {
            throw new ResourceNotFoundException("No audit records found for template ID: " + templateId);
        }
        return audits;
    }
}