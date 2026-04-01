package com.template.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "template_master")
public class TemplateMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String templateId;
    private String templateName;
    @Column(name = "message_type")
    private String messageType;
    private String version;

    @Column(columnDefinition = "json")
    private String alertConfig;

    @Column(columnDefinition = "json")
    private String headers;

	private String indexContent;

    @Column(name = "raw_content", columnDefinition = "json")
    private String rawContent;
    @Column(columnDefinition = "json")
    private String templateParams;

	@Column(columnDefinition = "json")
    private String paramMapping;


    private String isActive;

    private String createdBy;
    private String updatedBy;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

	public String getParamMapping() {
		return paramMapping;
	}

	public void setParamMapping(String paramMapping) {
		this.paramMapping = paramMapping;
	}

    public String getIndexContent() {
        return indexContent;
    }

    public void setIndexContent(String indexContent) {
        this.indexContent = indexContent;
    }

    public String getRawContent() {
        return rawContent;
    }

    public void setRawContent(String rawContent) {
        this.rawContent = rawContent;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getmessageType() {
        return messageType;
    }

    public void setmessageType(String messageType) {
        this.messageType = messageType;
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

    public String getHeaders() {
        return headers;
    }

    public void setHeaders(String headers) {
        this.headers = headers;
    }

    public String getindexContent() {
        return indexContent;
    }

    public void setindexContent(String indexContent) {
        this.indexContent = indexContent;
    }

    public String getTemplateParams() {
        return templateParams;
    }

    public void setTemplateParams(String templateParams) {
        this.templateParams = templateParams;
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
        builder.append("TemplateMaster [id=");
        builder.append(id);
        builder.append(", templateId=");
        builder.append(templateId);
        builder.append(", templateName=");
        builder.append(templateName);
        builder.append(", messageType=");
        builder.append(messageType);
        builder.append(", version=");
        builder.append(version);
        builder.append(", alertConfig=");
        builder.append(alertConfig);
        builder.append(", headers=");
        builder.append(headers);
        builder.append(", rawContent=");
        builder.append(rawContent);
        builder.append(", paramMapping=");
        builder.append(paramMapping);
        builder.append(", indexContent=");
        builder.append(indexContent);
        builder.append(", templateParams=");
        builder.append(templateParams);
        builder.append(", isActive=");
        builder.append(isActive);
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