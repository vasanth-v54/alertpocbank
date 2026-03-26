package com.notification.consumer.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "template_master")
public class TemplateMaster {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(columnDefinition = "json")
	private String templateIdentifiersRef;

	@Column(columnDefinition = "json")
	private String templateBody;

	private Boolean isActive;
	
	@Column(name = "template_id")
	private String templateid;

	private LocalDateTime createDate;
	private String createdBy;

	private LocalDateTime updateDate;
	private String updatedBy;
	
	private String messageType;
	
	public String getMessageType() {
		return messageType;
	}
	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getTemplateIdentifiersRef() {
		return templateIdentifiersRef;
	}
	public void setTemplateIdentifiersRef(String templateIdentifiersRef) {
		this.templateIdentifiersRef = templateIdentifiersRef;
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
		builder.append(", templateIdentifiersRef=");
		builder.append(templateIdentifiersRef);
		builder.append(", templateBody=");
		builder.append(templateBody);
		builder.append(", isActive=");
		builder.append(isActive);
		builder.append(", templateid=");
		builder.append(templateid);
		builder.append(", createDate=");
		builder.append(createDate);
		builder.append(", createdBy=");
		builder.append(createdBy);
		builder.append(", updateDate=");
		builder.append(updateDate);
		builder.append(", updatedBy=");
		builder.append(updatedBy);
		builder.append(", messageType=");
		builder.append(messageType);
		builder.append("]");
		return builder.toString();
	}
	public String getTemplateBody() {
		return templateBody;
	}
	public void setTemplateBody(String templateBody) {
		this.templateBody = templateBody;
	}
	public String getTemplateid() {
		return templateid;
	}
	public void setTemplateid(String templateid) {
		this.templateid = templateid;
	}
	
	

}