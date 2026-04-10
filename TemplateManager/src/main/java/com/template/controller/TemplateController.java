package com.template.controller;

import com.template.dto.*;
import com.template.service.TemplateService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/templates")
public class TemplateController {

    @Autowired
    private TemplateService templateService;

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
}
