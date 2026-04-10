package com.templatemaster.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.templatemaster.enums.MappingType;
import com.templatemaster.enums.MessageType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TemplateCreateRequest {

    @JsonProperty("template_name")
    @Valid
    @NotNull
    private TemplateName templateName;

    @JsonProperty("messageType")
    @NotNull
    private MessageType messageType;

    @JsonProperty("version")
    @NotBlank
    private String version;

    @JsonProperty("alert_config")
    @Valid
    @NotNull
    private AlertConfig alertConfig;

    @JsonProperty("headers")
    @Valid
    @NotNull
    private Headers headers;

    @JsonProperty("raw_content")
    @Valid
    @NotNull
    private RawContent rawContent;

    @JsonProperty("indexed_content")
    @Valid
    @NotNull
    private IndexedContent indexedContent;

    @JsonProperty("param_mapping")
    @Valid
    @NotNull
    private ParamMapping paramMapping;

    @JsonProperty("contentHash")
    private String contentHash;

    @JsonProperty("exceptionReason")
    private String exceptionReason;

    @JsonProperty("isDuplicateallowed")
    private Boolean duplicateAllowed = Boolean.FALSE;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TemplateName {
        @JsonProperty("email")
        private String email;

        @JsonProperty("sms")
        private String sms;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AlertConfig {
        @JsonProperty("domain")
        private String domain;

        @JsonProperty("alertName")
        private String alertName;

        @JsonProperty("alertId")
        private String alertId;

        @JsonProperty("eventType")
        private String eventType;

        @JsonProperty("alertType")
        private String alertType;

        @JsonProperty("originatingSource")
        private String originatingSource;

        @JsonProperty("leadCompanyCode")
        private String leadCompanyCode;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Headers {
        @JsonProperty("headers_email")
        private EmailHeaders headersEmail;

        @JsonProperty("headers_sms")
        private SmsHeaders headersSms;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EmailHeaders {
        @JsonProperty("subject")
        private String subject;

        @JsonProperty("from")
        private String from;

        @JsonProperty("to")
        private String to;

        @JsonProperty("cc")
        private String cc;

        @JsonProperty("bcc")
        private String bcc;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SmsHeaders {
        @JsonProperty("to")
        private String to;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RawContent {
        @JsonProperty("rawcontent_email")
        private String rawcontentEmail;

        @JsonProperty("rawcontent_sms")
        private String rawcontentSms;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class IndexedContent {
        @JsonProperty("indexed_content_email")
        private String indexedContentEmail;

        @JsonProperty("indexed_content_sms")
        private String indexedContentSms;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ParamMapping {
        @JsonProperty("param_mapping_email")
        private List<ParamItem> paramMappingEmail;

        @JsonProperty("param_mapping_sms")
        private List<ParamItem> paramMappingSms;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ParamItem {
        @JsonProperty("seq")
        private Integer seq;

        @JsonProperty("parameter")
        private String parameter;

        @JsonProperty("mappingType")
        private MappingType mappingType;

        @JsonProperty("augExpression")
        private String augExpression;
    }
}