package com.template.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TemplateUpdateRequestDTO {
    private String templateName;
    private String rawContent;
    private String alertConfig;
    private String headers;
    private String paramMapping;
    private String status;
    private Boolean isDuplicateAllowed;
    private String performedBy;
}
