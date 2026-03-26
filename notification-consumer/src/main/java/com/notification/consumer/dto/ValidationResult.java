package com.notification.consumer.dto;

public class ValidationResult {
    private boolean success;
    private String message;

    private ValidationResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public static ValidationResult success() {
        return new ValidationResult(true, "Validation Passed");
    }

    public static ValidationResult fail(String msg) {
        return new ValidationResult(false, msg);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}