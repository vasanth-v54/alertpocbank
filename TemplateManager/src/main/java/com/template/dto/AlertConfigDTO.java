package com.template.dto;

import lombok.Data;

@Data
public class AlertConfigDTO {
    private String domain;
    private String alertName;
    private String alertId;
    private String eventType;
    private String alertType;
    private String originatingSource;
    private String leadCompanyCode;
}
