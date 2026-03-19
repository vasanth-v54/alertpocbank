package com.poc.alerts.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "KEY_ROUTING_CONFIG")
public class KeyRoutingConfig {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "message_type")
	private String messageType;

	@Column(name = "alert_type")
	private String alertType;

	@Column(name = "event_type")
	private String eventType;

	@Column(name = "application")
	private String application;

	@Column(name = "sourceUri")
	private String sourceUri;

	@Column(name = "version")
	private Integer version;

	@Column(name = "is_active")
	private Boolean isActive;

	@Column(name = "created_by")
	private String createdBy;

	@Column(name = "created_on")
	private LocalDateTime createdOn;

	@Column(name = "updated_by")
	private String updatedBy;

	@Column(name = "updated_on")
	private LocalDateTime updatedOn;

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

	public String getAlertType() {
		return alertType;
	}

	public void setAlertType(String alertType) {
		this.alertType = alertType;
	}

	public String getEventType() {
		return eventType;
	}

	public void setEventType(String eventType) {
		this.eventType = eventType;
	}

	public String getApplication() {
		return application;
	}

	public void setApplication(String application) {
		this.application = application;
	}

	public String getSourceUri() {
		return sourceUri;
	}

	public void setSourceUri(String sourceUri) {
		this.sourceUri = sourceUri;
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
		builder.append("KeyRoutingConfig [id=");
		builder.append(id);
		builder.append(", messageType=");
		builder.append(messageType);
		builder.append(", alertType=");
		builder.append(alertType);
		builder.append(", eventType=");
		builder.append(eventType);
		builder.append(", application=");
		builder.append(application);
		builder.append(", sourceUri=");
		builder.append(sourceUri);
		builder.append(", version=");
		builder.append(version);
		builder.append(", isActive=");
		builder.append(isActive);
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