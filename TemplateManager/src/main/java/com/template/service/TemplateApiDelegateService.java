package com.template.service;

import com.template.api.ApiApiDelegate;
import com.template.api.model.TemplateAuditDto;
import com.template.entity.TemplateAudit;
import com.template.mapper.AuditMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TemplateApiDelegateService implements ApiApiDelegate {

    private final TemplateAuditService templateAuditService;
    private final AuditMapper auditMapper;

    public TemplateApiDelegateService(TemplateAuditService templateAuditService, AuditMapper auditMapper) {
        this.templateAuditService = templateAuditService;
        this.auditMapper = auditMapper;
    }

    @Override
    public ResponseEntity<List<TemplateAuditDto>> getTemplateAuditsById(Long templateId) {

        List<TemplateAudit> audits = templateAuditService.getAuditsByTemplateId(templateId);
        List<TemplateAuditDto> dtos = auditMapper.toDto(audits);
        return ResponseEntity.ok(dtos);
    }
}
