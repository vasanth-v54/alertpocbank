package com.template.controller;

import com.template.dto.*;
import com.template.service.TemplateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/v1/api/templates")
@RequiredArgsConstructor
public class TemplateController {

    private final TemplateService templateService;

    @PostMapping
    public ResponseEntity<?> createTemplate(@RequestBody TemplateRequest request) {

        String msg = templateService.createTemplate(request);

        return ResponseEntity.status(201).body(Map.of(
                "success", true,
                "message", msg
        ));
    }

    @GetMapping("/{templateName}")
    public ResponseEntity<ApiResponse<TemplateDetailResponseDTO>> getTemplateByName(
            @PathVariable String templateName) {

        TemplateDetailResponseDTO data = templateService.getTemplate(templateName);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Template fetched successfully",
                        data
                )
        );
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

    @PatchMapping("/toggle-status")
    public ResponseEntity<TemplateToggleStatusResponseDTO> toggleTemplateStatus(
            @Valid @RequestBody TemplateToggleStatusRequestDTO templateToggleStatusRequestDTO) {
        TemplateToggleStatusResponseDTO response = templateService.toggleTemplateStatus(templateToggleStatusRequestDTO);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/edit-template")
    public ResponseEntity<TemplateUpdateResponseDTO> updateTemplate(
            @Valid @RequestBody TemplateUpdateRequestDTO templateUpdateRequestDTO) {
        TemplateUpdateResponseDTO response = templateService.updateTemplate(templateUpdateRequestDTO);
        return ResponseEntity.ok(response);
    }
}