package com.template.dto;

import lombok.Data;
import java.util.List;

@Data
public class TemplateResponseDTO {

    private RoutingConfig routingConfig;
    private TemplateContent templateContent;

    private List<ParamDTO> emailParams;
    private List<ParamDTO> smsParams;
}