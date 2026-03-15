package com.poc.alerts.entity;

import java.util.Map;

public class DltLog {

    private String SessionId;
    private String CorrelationId;
    private String Environment;
    private String StatusCode;
    private String Severity;
    private String Application;
    private String Service;
    private String EventTimeUTC;
    private String BrowserType;
    private Map<String, Object> Data;

    public String getSessionId() {
        return SessionId;
    }

    public void setSessionId(String sessionId) {
        SessionId = sessionId;
    }

    public String getCorrelationId() {
        return CorrelationId;
    }

    public void setCorrelationId(String correlationId) {
        CorrelationId = correlationId;
    }

    public String getEnvironment() {
        return Environment;
    }

    public void setEnvironment(String environment) {
        Environment = environment;
    }

    public String getStatusCode() {
        return StatusCode;
    }

    public void setStatusCode(String statusCode) {
        StatusCode = statusCode;
    }

    public String getSeverity() {
        return Severity;
    }

    public void setSeverity(String severity) {
        Severity = severity;
    }

    public String getApplication() {
        return Application;
    }

    public void setApplication(String application) {
        Application = application;
    }

    public String getService() {
        return Service;
    }

    public void setService(String service) {
        Service = service;
    }

    public String getEventTimeUTC() {
        return EventTimeUTC;
    }

    public void setEventTimeUTC(String eventTimeUTC) {
        EventTimeUTC = eventTimeUTC;
    }

    public String getBrowserType() {
        return BrowserType;
    }

    public void setBrowserType(String browserType) {
        BrowserType = browserType;
    }

    public Map<String, Object> getData() {
        return Data;
    }

    public void setData(Map<String, Object> data) {
        Data = data;
    }
}