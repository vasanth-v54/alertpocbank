package com.template.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class ParamDTO {
    private int seq;
    private String parameter;
    private String mappingType;
    private String augExpression;
}