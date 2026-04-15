package com.template.dto;

import com.template.enums.DXP_Status;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class TemplateToggleDataDTO {
    private List<String> createdTemplateNames;
    private DXP_Status newStatus;
}