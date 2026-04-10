package com.templatemaster.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum MessageType {
    SMS,
    EMAIL,
    BOTH;

    @JsonCreator
    public static MessageType from(String value) {
        if (value == null) {
            return null;
        }
        return MessageType.valueOf(value.trim().toUpperCase());
    }
}
