package com.notification.consumer.dto;

import java.util.Map;

public class NotificationRequest {

    private Map<String, String> keyRequest;
    private String result;

    // Default Constructor
    public NotificationRequest() {
    }

    // Parameterized Constructor
    public NotificationRequest(Map<String, String> keyRequest, String result) {
        this.keyRequest = keyRequest;
        this.result = result;
    }

    // Getters and Setters
    public Map<String, String> getKeyRequest() {
        return keyRequest;
    }

    public void setKeyRequest(Map<String, String> keyRequest) {
        this.keyRequest = keyRequest;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    // toString (useful for logging)
    @Override
    public String toString() {
        return "NotificationRequest{" +
                "keyRequest=" + keyRequest +
                ", result='" + result + '\'' +
                '}';
    }
}
