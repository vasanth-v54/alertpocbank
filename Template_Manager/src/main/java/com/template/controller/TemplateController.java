package com.template.controller;

import com.template.dto.ApiResponse;
import com.template.dto.TemplateCreateData;
import com.template.dto.TemplateCreateRequest;
import com.template.service.TemplateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/api/templates")
@RequiredArgsConstructor
public class TemplateController {

    private final TemplateService templateService;

    @PostMapping
    public ResponseEntity<ApiResponse<TemplateCreateData>> createTemplate(
            @Valid @RequestBody TemplateCreateRequest request
    ) throws Exception {
        return ResponseEntity.status(201).body(templateService.createTemplate(request));
    }
}