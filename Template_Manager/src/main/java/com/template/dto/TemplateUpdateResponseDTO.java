package com.template.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TemplateUpdateResponseDTO {
    private Boolean success;
    private String message;
    private Long id;
    private String templateId;
    private String version;
    private String status;
}
