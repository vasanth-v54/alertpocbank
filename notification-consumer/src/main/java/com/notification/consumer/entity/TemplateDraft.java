package com.notification.consumer.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "TM_DRAFT_WORK")
public class TemplateDraft {

    @Id
    @Column(name = "draft_id")
    private String draftId;

    @Column(name = "template_id")
    private String templateId;

    @Column(name = "template_name", nullable = false)
    private String templateName;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false)
    private Channel channel;

    @Column(name = "version", nullable = false)
    private String version;

    @Column(name = "alert_config", columnDefinition = "json")
    private String alertConfig;

    @Column(name = "email_fields", columnDefinition = "json")
    private String emailFields;

    @Column(name = "sms_fields", columnDefinition = "json")
    private String smsFields;

    @Column(name = "raw_content", columnDefinition = "longtext")
    private String rawContent;

    @Column(name = "indexed_content", columnDefinition = "longtext")
    private String indexedContent;

    @Column(name = "param_mapping", columnDefinition = "json")
    private String paramMapping;

    @Enumerated(EnumType.STRING)
    @Column(name = "is_template_active")
    private TemplateStatus status;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

	public String getDraftId() {
		return draftId;
	}

	public void setDraftId(String draftId) {
		this.draftId = draftId;
	}

	public String getTemplateId() {
		return templateId;
	}

	public void setTemplateId(String templateId) {
		this.templateId = templateId;
	}

	public String getTemplateName() {
		return templateName;
	}

	public void setTemplateName(String templateName) {
		this.templateName = templateName;
	}

	public Channel getChannel() {
		return channel;
	}

	public void setChannel(Channel channel) {
		this.channel = channel;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getAlertConfig() {
		return alertConfig;
	}

	public void setAlertConfig(String alertConfig) {
		this.alertConfig = alertConfig;
	}

	public String getEmailFields() {
		return emailFields;
	}

	public void setEmailFields(String emailFields) {
		this.emailFields = emailFields;
	}

	public String getSmsFields() {
		return smsFields;
	}

	public void setSmsFields(String smsFields) {
		this.smsFields = smsFields;
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

	public TemplateStatus getStatus() {
		return status;
	}

	public void setStatus(TemplateStatus status) {
		this.status = status;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public String getUpdatedBy() {
		return updatedBy;
	}

	public void setUpdatedBy(String updatedBy) {
		this.updatedBy = updatedBy;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("TemplateDraft [draftId=");
		builder.append(draftId);
		builder.append(", templateId=");
		builder.append(templateId);
		builder.append(", templateName=");
		builder.append(templateName);
		builder.append(", channel=");
		builder.append(channel);
		builder.append(", version=");
		builder.append(version);
		builder.append(", alertConfig=");
		builder.append(alertConfig);
		builder.append(", emailFields=");
		builder.append(emailFields);
		builder.append(", smsFields=");
		builder.append(smsFields);
		builder.append(", rawContent=");
		builder.append(rawContent);
		builder.append(", indexedContent=");
		builder.append(indexedContent);
		builder.append(", paramMapping=");
		builder.append(paramMapping);
		builder.append(", status=");
		builder.append(status);
		builder.append(", createdBy=");
		builder.append(createdBy);
		builder.append(", updatedBy=");
		builder.append(updatedBy);
		builder.append(", createdAt=");
		builder.append(createdAt);
		builder.append(", updatedAt=");
		builder.append(updatedAt);
		builder.append("]");
		return builder.toString();
	}
    
	
}