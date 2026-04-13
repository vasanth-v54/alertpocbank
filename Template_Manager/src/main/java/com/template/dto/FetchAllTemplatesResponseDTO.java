package com.template.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FetchAllTemplatesResponseDTO {

    private String templateName;

    private String domain;
    private String alertName;
    private String alertType;
    private String eventType;

    private String msgType;

    private String smsTemplate;
    private String emailTemplate;
}