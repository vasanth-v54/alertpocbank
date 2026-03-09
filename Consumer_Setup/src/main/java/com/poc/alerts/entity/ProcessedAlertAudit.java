package com.poc.alerts.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="PROCESSED_ALERT_AUDIT")
public class ProcessedAlertAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="MESSAGE_TYPE")
    private String messageType;

    @Column(name="MESSAGE", columnDefinition = "TEXT")
    private String message;

    @Column(name="EVENT_ID")
    private String eventId;

    @Column(name="ALERTTYPE")
    private String alertType;

    @Column(name="CREATED_ON")
    private LocalDateTime createdOn;

    @Column(name="CREATED_BY")
    private String createdBy;

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public void setAlertType(String alertType) {
        this.alertType = alertType;
    }

    public void setCreatedOn(LocalDateTime createdOn) {
        this.createdOn = createdOn;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getMessageType() {
		return messageType;
	}

	public String getMessage() {
		return message;
	}

	public String getEventId() {
		return eventId;
	}

	public String getAlertType() {
		return alertType;
	}

	public LocalDateTime getCreatedOn() {
		return createdOn;
	}

	public String getCreatedBy() {
		return createdBy;
	}
    
    
}