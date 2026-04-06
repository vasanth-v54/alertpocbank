package com.template.dto;

import lombok.Data;

import java.util.List;

@Data
public class ParamMappingDTO {
    private List<ParamDTO> param_mapping_email;
    private List<ParamDTO> param_mapping_sms;
}
