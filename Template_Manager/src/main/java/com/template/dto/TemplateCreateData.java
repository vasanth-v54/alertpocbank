package com.template.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TemplateCreateData {
    private String messageType;
    private String version;
    private List<String> createdTemplateNames;
}