package com.template.dto;

import lombok.Data;

@Data
public class EmailHeadersDTO {
    private String subject;
    private String from;
    private String to;
    private String cc;
    private String bcc;
}