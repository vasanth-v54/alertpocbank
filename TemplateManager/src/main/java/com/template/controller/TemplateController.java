package com.template.controller;

import com.template.dto.ApiResponse;
import com.template.dto.TemplateResponseDTO;
import com.template.service.TemplateService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/templates")
@RequiredArgsConstructor
public class TemplateController {

    private final TemplateService service;

    @GetMapping("/{templateName}")
    public ResponseEntity<ApiResponse<TemplateResponseDTO>> getTemplate(
            @PathVariable String templateName) {
        try {
            TemplateResponseDTO data = service.getTemplateByTemplateName(templateName);
            return ResponseEntity.ok(ApiResponse.success(data));
        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.failure(e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TemplateResponseDTO>>> getAllTemplates() {
        try {
            List<TemplateResponseDTO> data = service.getAllTemplates();
            return ResponseEntity.ok(ApiResponse.success(data));
        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.failure(e.getMessage()));
        }
    }
}
