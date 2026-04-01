package com.template.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "TM_TEMPLATE_AUDIT")
public class TemplateAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "audit_id")
    private String auditId;

    @Column(name = "template_id", nullable = false)
    private Long templateId;

    @Column(name = "template_name", nullable = false)
    private String templateName;

    @Column(name = "version", nullable = false)
    private String version;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false)
    private Channel channel;

    @Column(name = "indexed_content", columnDefinition = "LONGTEXT")
    private String indexedContent;

    @Column(name = "param_mapping", columnDefinition = "json")
    private String paramMapping;

    @Enumerated(EnumType.STRING)
    @Column(name = "is_template_active", nullable = false)
    private TemplateStatus isTemplateActive;

    @Column(name = "changed_by")
    private String changedBy;

    @Column(name = "changed_at", nullable = false, updatable = false, insertable = false)
    private LocalDateTime changedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "change_reason", nullable = false)
    private ChangeReason changeReason;

    public enum Channel {
        EMAIL, SMS, BOTH
    }

    public enum TemplateStatus {
        DRAFT, ACTIVE, INACTIVE
    }

    public enum ChangeReason {
        CREATED, EDITED, DEACTIVATED
    }

    // Getters and setters

    public String getAuditId() {
        return auditId;
    }

    public void setAuditId(String auditId) {
        this.auditId = auditId;
    }

    public Long getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Long templateId) {
        this.templateId = templateId;
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

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
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

    public TemplateStatus getIsTemplateActive() {
        return isTemplateActive;
    }

    public void setIsTemplateActive(TemplateStatus isTemplateActive) {
        this.isTemplateActive = isTemplateActive;
    }

    public String getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(String changedBy) {
        this.changedBy = changedBy;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(LocalDateTime changedAt) {
        this.changedAt = changedAt;
    }

    public ChangeReason getChangeReason() {
        return changeReason;
    }

    public void setChangeReason(ChangeReason changeReason) {
        this.changeReason = changeReason;
    }
}