package com.template.dto;

import lombok.Data;
import java.util.Map;

@Data
public class TemplateDetailResponseDTO {

    private String template_name;
    private String messageType;
    private String version;
    private Object id;

    private Map<String, Object> alert_config;
    private Map<String, Object> headers;
    private Map<String, Object> raw_content;
    private Map<String, Object> indexed_content;
    private Map<String, Object> param_mapping;

    private String contentHash;
    private String exceptionReason;
    private Boolean isDuplicateallowed;

}