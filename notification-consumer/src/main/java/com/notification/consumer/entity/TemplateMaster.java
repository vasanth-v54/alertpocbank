package com.notification.consumer.entity;

import java.time.LocalDateTime;
import java.util.Map;

import jakarta.persistence.*;

import org.hibernate.annotations.Type;

import com.vladmihalcea.hibernate.type.json.JsonType;


@Entity
@Table(name = "template_master")
public class TemplateMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "template_name")
    private String templateName;

    @Column(name = "messageType")
    private String messageType;

    @Column(name = "version")
    private String version;

    @Type(JsonType.class)
    @Column(name = "alert_config", columnDefinition = "json")
    private Map<String, Object> alertConfig;

    @Type(JsonType.class)
    @Column(name = "headers", columnDefinition = "json")
    private Map<String, Object> headers;

    @Type(JsonType.class)
    @Column(name = "raw_content", columnDefinition = "json")
    private Map<String, Object> rawContent;

    @Type(JsonType.class)
    @Column(name = "indexed_content", columnDefinition = "json")
    private Map<String, Object> indexedContent;

    @Type(JsonType.class)
    @Column(name = "param_mapping", columnDefinition = "json")
    private Map<String, Object> paramMapping;

    @Column(name = "contentHash")
    private String contentHash;

    @Column(name = "exceptionReason")
    private String exceptionReason;

    @Column(name = "isDuplicateallowed")
    private boolean isDuplicateallowed;

    @Column(name = "IS_ACTIVE")
    private Integer isActive;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "modified_by")
    private String modifiedBy;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "modified_date")
    private LocalDateTime modifiedDate;

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

	public Map<String, Object> getAlertConfig() {
		return alertConfig;
	}

	public void setAlertConfig(Map<String, Object> alertConfig) {
		this.alertConfig = alertConfig;
	}

	public Map<String, Object> getHeaders() {
		return headers;
	}

	public void setHeaders(Map<String, Object> headers) {
		this.headers = headers;
	}

	public Map<String, Object> getRawContent() {
		return rawContent;
	}

	public void setRawContent(Map<String, Object> rawContent) {
		this.rawContent = rawContent;
	}

	public Map<String, Object> getIndexedContent() {
		return indexedContent;
	}

	public void setIndexedContent(Map<String, Object> indexedContent) {
		this.indexedContent = indexedContent;
	}

	public Map<String, Object> getParamMapping() {
		return paramMapping;
	}

	public void setParamMapping(Map<String, Object> paramMapping) {
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

	public boolean isDuplicateallowed() {
		return isDuplicateallowed;
	}

	public void setDuplicateallowed(boolean isDuplicateallowed) {
		this.isDuplicateallowed = isDuplicateallowed;
	}

	public Integer getIsActive() {
		return isActive;
	}

	public void setIsActive(Integer isActive) {
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

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("TemplateMaster [id=");
		builder.append(id);
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
		builder.append(", indexedContent=");
		builder.append(indexedContent);
		builder.append(", paramMapping=");
		builder.append(paramMapping);
		builder.append(", contentHash=");
		builder.append(contentHash);
		builder.append(", exceptionReason=");
		builder.append(exceptionReason);
		builder.append(", isDuplicateallowed=");
		builder.append(isDuplicateallowed);
		builder.append(", isActive=");
		builder.append(isActive);
		builder.append(", createdBy=");
		builder.append(createdBy);
		builder.append(", modifiedBy=");
		builder.append(modifiedBy);
		builder.append(", createdDate=");
		builder.append(createdDate);
		builder.append(", modifiedDate=");
		builder.append(modifiedDate);
		builder.append("]");
		return builder.toString();
	}

    // getters/setters unchanged
    
    
}
