package com.template.controller;

import com.template.dto.TemplateResponseDTO;
import com.template.service.TemplateService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/templates")
@RequiredArgsConstructor
public class TemplateController {

    private final TemplateService service;

    @GetMapping("/{templateName}")
    public ResponseEntity<TemplateResponseDTO> getTemplate(
            @PathVariable String templateName) {

        return ResponseEntity.ok(service.getTemplate(templateName));
    }
    }

   /* @GetMapping
    public ResponseEntity<ApiResponse<List<TemplateResponseDTO>>> getAllTemplates() {
        try {
            List<TemplateResponseDTO> data = service.getAllTemplates();
            return ResponseEntity.ok(ApiResponse.success(data));
        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.failure(e.getMessage()));
        }
    }*/

