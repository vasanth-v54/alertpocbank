package com.template.dto;

import lombok.Data;
import java.util.List;

@Data
public class TemplateResponseDTO {
    private boolean success;
    private String message;
    private DataDTO data;
}