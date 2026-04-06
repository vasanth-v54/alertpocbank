package com.template.dto;

import lombok.Data;

@Data
public class HeadersDTO {
    private EmailHeadersDTO headers_email;
    private SmsHeadersDTO headers_sms; // for SMS
}