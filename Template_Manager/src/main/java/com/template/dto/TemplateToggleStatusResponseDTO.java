package com.template.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TemplateToggleStatusResponseDTO {
    private boolean success;
    private String message;
    private Long id;
    private String newStatus;
}