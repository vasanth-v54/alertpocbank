package com.template.dto;

import lombok.Data;

@Data
public class DataDTO {

    private String template_name;
    private String messageType;
    private String version;

    private AlertConfigDTO alert_config;
    private HeadersDTO headers;
    private RawContentDTO raw_content;
    private IndexedContentDTO indexed_content;
    private ParamMappingDTO param_mapping;

    private String contentHash;
    private String exceptionReason;
    private boolean isDuplicateallowed;

    public String getTemplate_name() {
        return template_name;
    }

    public void setTemplate_name(String template_name) {
        this.template_name = template_name;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public AlertConfigDTO getAlert_config() {
        return alert_config;
    }

    public void setAlert_config(AlertConfigDTO alert_config) {
        this.alert_config = alert_config;
    }

    public HeadersDTO getHeaders() {
        return headers;
    }

    public void setHeaders(HeadersDTO headers) {
        this.headers = headers;
    }

    public RawContentDTO getRaw_content() {
        return raw_content;
    }

    public void setRaw_content(RawContentDTO raw_content) {
        this.raw_content = raw_content;
    }

    public IndexedContentDTO getIndexed_content() {
        return indexed_content;
    }

    public void setIndexed_content(IndexedContentDTO indexed_content) {
        this.indexed_content = indexed_content;
    }

    public ParamMappingDTO getParam_mapping() {
        return param_mapping;
    }

    public void setParam_mapping(ParamMappingDTO param_mapping) {
        this.param_mapping = param_mapping;
    }

    public String getContentHash() {
        return contentHash;
    }

    public void setContentHash(String contentHash) {
        this.contentHash = contentHash;
    }

    public String getExceptionReason() {
        return exceptionReason;
    }

    public void setExceptionReason(String exceptionReason) {
        this.exceptionReason = exceptionReason;
    }

    public boolean isDuplicateallowed() {
        return isDuplicateallowed;
    }

    public void setIsDuplicateallowed(boolean duplicateallowed) {
        isDuplicateallowed = duplicateallowed;
    }
}
