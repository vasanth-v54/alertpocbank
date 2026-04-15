package com.template.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum DXP_Status {
    ACTIVE,
    INACTIVE;

//    @JsonCreator
//    public static DXP_Status fromValue(String value) {
//        return value == null ? null : DXP_Status.valueOf(value.toUpperCase());
//    }
    @JsonCreator
    public static DXP_Status fromValue(String value) {
        if (value == null) return null;

        try {
            return DXP_Status.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new RuntimeException("Invalid status value: " + value);
        }
    }
}