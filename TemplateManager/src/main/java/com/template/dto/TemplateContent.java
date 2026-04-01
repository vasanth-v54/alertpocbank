package com.template.dto;
import lombok.Data;
import java.util.Map;

@Data
public class TemplateContent {

    private String emailContent;
    private String smsContent;
    private Map<String, Object> emailHeaders;
    private Map<String, Object> smsHeaders;
    private String version;
}