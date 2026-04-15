package com.template.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.template.enums.DXP_Status;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TemplateToggleStatusRequestDTO {


    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private DXP_Status status;

    @Valid
    @NotNull(message = "template_name is required")
    private TemplateName template_name;

    @NotBlank(message = "PerformedBy is required.")
    private String performedBy;

    @NotBlank(message = "Version is required.")
    private String version;
}