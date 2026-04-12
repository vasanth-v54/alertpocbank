package com.template.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "template_master")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TemplateMaster {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "template_name", nullable = false)
	private String templateName;

  @Enumerated(EnumType.STRING)
	@Column(name = "messageType", nullable = false)
	private Channel messageType;

	@Column(name = "version", nullable = false)
	private String version;

	@Column(name = "alert_config", columnDefinition = "json", nullable = false)
	private String alertConfig;

	@Column(name = "headers", columnDefinition = "json", nullable = false)
	private String headers;

	@Column(name = "raw_content", columnDefinition = "json", nullable = false)
	private String rawContent;

	@Column(name = "indexed_content", columnDefinition = "json", nullable = false)
	private String indexedContent;

	@Column(name = "param_mapping", columnDefinition = "json", nullable = false)
	private String paramMapping;

	@Column(name = "contentHash")
	private String contentHash;

	@Column(name = "exceptionReason")
	private String exceptionReason;

	@Column(name = "isDuplicateallowed", nullable = false)
	private Boolean isDuplicateAllowed;

	@Column(name = "IS_ACTIVE", nullable = false)
	private Boolean isActive;

	@Column(name = "created_by", nullable = false)
	private String createdBy;

	@Column(name = "modified_by")
	private String modifiedBy;

	@Column(name = "created_date")
	private LocalDateTime createdDate;

	@Column(name = "modified_date")
	private LocalDateTime modifiedDate;

    public enum Channel {
        EMAIL, SMS, BOTH
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

	public Channel getMessageType() {
		return messageType;
	}

	public void setMessageType(Channel messageType) {
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

	public Boolean getIsDuplicateAllowed() {
		return isDuplicateAllowed;
	}

	public void setIsDuplicateAllowed(Boolean isDuplicateAllowed) {
		this.isDuplicateAllowed = isDuplicateAllowed;
	}

	public Boolean getIsActive() {
		return isActive;
	}

	public void setIsActive(Boolean isActive) {
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
		builder.append(", isDuplicateAllowed=");
		builder.append(isDuplicateAllowed);
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


}