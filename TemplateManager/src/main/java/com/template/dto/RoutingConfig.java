package com.template.dto;
import lombok.Data;

@Data
public class RoutingConfig {

    private String templateName;
    private String alertId;
    private String alertName;
    private String domain;
    private String eventType;
    private String alertType;
    private String messageType;
    private String status;
    private String version;
}