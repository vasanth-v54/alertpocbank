package com.poc.alerts.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="TEMPLATE_MST")
public class TemplateMst {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="template_id")
    private String templateId;

    @Column(name="message_type")
    private String messageType;

    @Column(name="alert_type")
    private String alertType;

    @Column(name = "template_variable", columnDefinition = "json")
    private String templateVariable;

    @Column(name="version")
    private Integer version;

    @Column(name="is_active")
    private Boolean isActive;

    @Column(name="flexi_field1")
    private String flexiField1;

    @Column(name="flexi_field2")
    private String flexiField2;

    @Column(name="flexi_field3")
    private String flexiField3;

    @Column(name="created_by")
    private String createdBy;

    @Column(name="created_on")
    private LocalDateTime createdOn;

    @Column(name="updated_by")
    private String updatedBy;

    @Column(name="updated_on")
    private LocalDateTime updatedOn;

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

	public String getMessageType() {
		return messageType;
	}

	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}

	public String getAlertType() {
		return alertType;
	}

	public void setAlertType(String alertType) {
		this.alertType = alertType;
	}

	public String getTemplateVariable() {
		return templateVariable;
	}

	public void setTemplateVariable(String templateVariable) {
		this.templateVariable = templateVariable;
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	public Boolean getIsActive() {
		return isActive;
	}

	public void setIsActive(Boolean isActive) {
		this.isActive = isActive;
	}

	public String getFlexiField1() {
		return flexiField1;
	}

	public void setFlexiField1(String flexiField1) {
		this.flexiField1 = flexiField1;
	}

	public String getFlexiField2() {
		return flexiField2;
	}

	public void setFlexiField2(String flexiField2) {
		this.flexiField2 = flexiField2;
	}

	public String getFlexiField3() {
		return flexiField3;
	}

	public void setFlexiField3(String flexiField3) {
		this.flexiField3 = flexiField3;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public LocalDateTime getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(LocalDateTime createdOn) {
		this.createdOn = createdOn;
	}

	public String getUpdatedBy() {
		return updatedBy;
	}

	public void setUpdatedBy(String updatedBy) {
		this.updatedBy = updatedBy;
	}

	public LocalDateTime getUpdatedOn() {
		return updatedOn;
	}

	public void setUpdatedOn(LocalDateTime updatedOn) {
		this.updatedOn = updatedOn;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("TemplateMst [id=");
		builder.append(id);
		builder.append(", templateId=");
		builder.append(templateId);
		builder.append(", messageType=");
		builder.append(messageType);
		builder.append(", alertType=");
		builder.append(alertType);
		builder.append(", templateVariable=");
		builder.append(templateVariable);
		builder.append(", version=");
		builder.append(version);
		builder.append(", isActive=");
		builder.append(isActive);
		builder.append(", flexiField1=");
		builder.append(flexiField1);
		builder.append(", flexiField2=");
		builder.append(flexiField2);
		builder.append(", flexiField3=");
		builder.append(flexiField3);
		builder.append(", createdBy=");
		builder.append(createdBy);
		builder.append(", createdOn=");
		builder.append(createdOn);
		builder.append(", updatedBy=");
		builder.append(updatedBy);
		builder.append(", updatedOn=");
		builder.append(updatedOn);
		builder.append("]");
		return builder.toString();
	}

	
    
}