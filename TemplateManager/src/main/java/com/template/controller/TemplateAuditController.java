package com.template.controller;

import com.template.api.model.TemplateAuditDto;
import com.template.entity.TemplateAudit;
import com.template.mapper.AuditMapper;
import com.template.service.TemplateAuditService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/templates")
public class TemplateAuditController {

    private final TemplateAuditService service;
    private final AuditMapper auditMapper; // Inject the mapper

    public TemplateAuditController(TemplateAuditService service, AuditMapper auditMapper) {
        this.service = service;
        this.auditMapper = auditMapper;
    }

    @GetMapping("/{id}/audit")
    public List<TemplateAuditDto> getTemplateAudits(@PathVariable("id") Long templateId) {
        List<TemplateAudit> audits = service.getAuditsByTemplateId(templateId);
        return auditMapper.toDto(audits);
    }
}
