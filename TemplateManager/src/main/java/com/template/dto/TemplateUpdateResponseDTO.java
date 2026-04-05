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
    private Long id;
    private String version;
    private String status;
    private String message;
}
