package com.template.dto;

import com.template.enums.DXP_Status;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TemplateToggleStatusRequestDTO {

    private DXP_Status status;

    @NotBlank(message = "PerformedBy is required.")
    private String performedBy;
}