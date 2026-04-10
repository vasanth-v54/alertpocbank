package com.templatemaster.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum MappingType {
    DIRECT,
    DERIVED;

    @JsonCreator
    public static MappingType from(String value) {
        if (value == null) {
            return null;
        }
        return MappingType.valueOf(value.trim().toUpperCase());
    }
}