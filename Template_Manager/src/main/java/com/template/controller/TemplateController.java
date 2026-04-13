package com.template.controller;

import com.template.dto.*;
import com.template.service.TemplateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<FetchAllTemplatesResponseDTO>>> fetchAllTemplates() {

        List<FetchAllTemplatesResponseDTO> data = templateService.fetchAllTemplates();

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Templates fetched successfully",
                        data
                )
        );
    }
}