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
    private TemplateName template_name;
    private String messageType;
    private String version;
    private Object alert_config;
    private Object headers;
    private Object raw_content;
    private Object indexed_content;
    private Object param_mapping;
    private String contentHash;
    private String exceptionReason;
    private Boolean isDuplicateallowed;
    private String status;
    private String createdBy;
}
