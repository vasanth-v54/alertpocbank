package com.template.controller;

import com.template.api.model.TemplateAuditDto;
import com.template.dto.*;
import com.template.entity.TemplateAudit;
import com.template.mapper.AuditMapper;
import com.template.service.TemplateAuditService;
import com.template.service.TemplateService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/templates")
public class TemplateController {

    @Autowired
    private TemplateService templateService;

    private final TemplateAuditService auditService;
    private final AuditMapper auditMapper;

    public TemplateController(TemplateService templateService, TemplateAuditService auditService, AuditMapper auditMapper) {
        this.templateService = templateService;
        this.auditService = auditService;
        this.auditMapper = auditMapper;
    }

    @PostMapping("/{id}/toggle-status")
    public ResponseEntity<TemplateToggleStatusResponseDTO> deactivateTemplate(
            @PathVariable Long id,
            @Valid @RequestBody TemplateToggleStatusRequestDTO templateToggleStatusRequestDTO) {
        TemplateToggleStatusResponseDTO response = templateService.toggleTemplateStatus(id, templateToggleStatusRequestDTO);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TemplateUpdateResponseDTO> updateTemplate(
            @PathVariable Long id,
            @Valid @RequestBody TemplateUpdateRequestDTO templateUpdateRequestDTO) {
        TemplateUpdateResponseDTO response = templateService.updateTemplate(id, templateUpdateRequestDTO);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/audit")
    public List<TemplateAuditDto> getTemplateAudits(@PathVariable("id") Long templateId) {
        List<TemplateAudit> audits = auditService.getAuditsByTemplateId(templateId);
        return auditMapper.toDto(audits);
    }
}
