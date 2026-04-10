package com.template.dto;

import com.template.enums.DXP_Status;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TemplateToggleStatusResponseDTO {
    private boolean success;
    private Long id;
    private DXP_Status newStatus;

}
