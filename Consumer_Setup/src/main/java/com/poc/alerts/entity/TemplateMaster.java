package com.poc.alerts.entity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.hibernate.annotations.Type;
import com.vladmihalcea.hibernate.type.json.JsonType;
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

    @Column(name = "message_type", nullable = false)
    private String messageType;

    @Type(JsonType.class)
    @Column(name = "template_identifiers_ref", columnDefinition = "json", nullable = false)
    private Map<String, Object> templateIdentifiersRef;

    @Type(JsonType.class)
    @Column(name = "template_parameters", columnDefinition = "json", nullable = false)
    private List<String> templateParameters;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "create_date", updatable = false)
    private LocalDateTime createDate;

    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @Column(name = "update_date")
    private LocalDateTime updateDate;

    @Column(name = "updated_by")
    private String updatedBy;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getMessageType() {
		return messageType;
	}

	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}

	public Map<String, Object> getTemplateIdentifiersRef() {
		return templateIdentifiersRef;
	}

	public void setTemplateIdentifiersRef(Map<String, Object> templateIdentifiersRef) {
		this.templateIdentifiersRef = templateIdentifiersRef;
	}

	public List<String> getTemplateParameters() {
		return templateParameters;
	}

	public void setTemplateParameters(List<String> templateParameters) {
		this.templateParameters = templateParameters;
	}

	public Boolean getIsActive() {
		return isActive;
	}

	public void setIsActive(Boolean isActive) {
		this.isActive = isActive;
	}

	public LocalDateTime getCreateDate() {
		return createDate;
	}

	public void setCreateDate(LocalDateTime createDate) {
		this.createDate = createDate;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public LocalDateTime getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(LocalDateTime updateDate) {
		this.updateDate = updateDate;
	}

	public String getUpdatedBy() {
		return updatedBy;
	}

	public void setUpdatedBy(String updatedBy) {
		this.updatedBy = updatedBy;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("TemplateMaster [id=");
		builder.append(id);
		builder.append(", messageType=");
		builder.append(messageType);
		builder.append(", templateIdentifiersRef=");
		builder.append(templateIdentifiersRef);
		builder.append(", templateParameters=");
		builder.append(templateParameters);
		builder.append(", isActive=");
		builder.append(isActive);
		builder.append(", createDate=");
		builder.append(createDate);
		builder.append(", createdBy=");
		builder.append(createdBy);
		builder.append(", updateDate=");
		builder.append(updateDate);
		builder.append(", updatedBy=");
		builder.append(updatedBy);
		builder.append("]");
		return builder.toString();
	}
    
    
}
