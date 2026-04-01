package com.template.controller;

import com.template.entity.TemplateAudit;
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

    public TemplateAuditController(TemplateAuditService service) {
        this.service = service;
    }

    @GetMapping("/{id}/audit")
    public List<TemplateAudit> getTemplateAudits(@PathVariable("id") String templateId) {
        return service.getAuditsByTemplateId(templateId);
    }
}