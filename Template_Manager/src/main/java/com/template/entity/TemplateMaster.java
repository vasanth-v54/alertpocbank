package com.template.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(
        name = "template_master",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_template",
                columnNames = {"template_name", "message_type", "is_active"}
        )
)
@Data
public class TemplateMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "template_name", nullable = false)
    private String templateName;

    @Column(name = "version")
    private String version;

    @Column(name = "message_type")
    private String messageType;

    @Column(name = "alert_config", columnDefinition = "json")
    private String alertConfig;

    @Column(name = "headers", columnDefinition = "json")
    private String headers;

    @Column(name = "raw_content", columnDefinition = "json")
    private String rawContent;

    @Column(name = "indexed_content", columnDefinition = "json")
    private String indexedContent;

    @Column(name = "param_mapping", columnDefinition = "json")
    private String paramMapping;

    @Column(name = "contentHash")
    private String contentHash;

    @Column(name = "exceptionReason")
    private String exceptionReason;

    @Column(name = "isDuplicateallowed")
    private Boolean duplicateAllowed;

    @Column(name = "is_active")
    private String isActive;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "modified_by")
    private String modifiedBy;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "modified_date")
    private LocalDateTime modifiedDate;

    @PreUpdate
    public void preUpdate() {
        modifiedDate = LocalDateTime.now();
        if (modifiedBy == null || modifiedBy.isBlank()) modifiedBy = "SYSTEM";
    }
    @PrePersist
    public void prePersist() {
        if (isActive == null || isActive.isBlank()) {
            isActive = "1";   // ✅ default
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getAlertConfig() {
        return alertConfig;
    }

    public void setAlertConfig(String alertConfig) {
        this.alertConfig = alertConfig;
    }

    public String getHeaders() {
        return headers;
    }

    public void setHeaders(String headers) {
        this.headers = headers;
    }

    public String getRawContent() {
        return rawContent;
    }

    public void setRawContent(String rawContent) {
        this.rawContent = rawContent;
    }

    public String getIndexedContent() {
        return indexedContent;
    }

    public void setIndexedContent(String indexedContent) {
        this.indexedContent = indexedContent;
    }

    public String getParamMapping() {
        return paramMapping;
    }

    public void setParamMapping(String paramMapping) {
        this.paramMapping = paramMapping;
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

    public Boolean getDuplicateAllowed() {
        return duplicateAllowed;
    }

    public void setDuplicateAllowed(Boolean duplicateAllowed) {
        this.duplicateAllowed = duplicateAllowed;
    }

    public String getIsActive() {
        return isActive;
    }

    public void setIsActive(String isActive) {
        this.isActive = isActive;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public LocalDateTime getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(LocalDateTime modifiedDate) {
        this.modifiedDate = modifiedDate;
    }
}