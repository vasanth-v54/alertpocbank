package com.poc.alerts.entity;

import java.time.LocalDateTime;



import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
@Entity
@Table(name="consumer_entry_audit")
public class ConsumerEntryAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="topic")
    private String topic;

    @Column(name="business_key")
    private String businessKey;

    @Column(name="source_uri")
    private String sourceUri;

    @Column(name="status")
    private String status;

    @Column(name="eventtype")
    private String eventType;

    @Column(name="event_id")
    private String eventId;

    @Column(name="application")
    private String application;

    @Column(name="messagetype")
    private String messageType;

    @Column(name="alerttype")
    private String alertType;

    @Column(name="release_version")
    private String releaseVersion;

    @Column(name="received_time")
    private LocalDateTime receivedTime;

    @Column(name="full_payload", columnDefinition="JSON")
    private String fullPayload;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public String getBusinessKey() {
		return businessKey;
	}

	public void setBusinessKey(String businessKey) {
		this.businessKey = businessKey;
	}

	public String getSourceUri() {
		return sourceUri;
	}

	public void setSourceUri(String sourceUri) {
		this.sourceUri = sourceUri;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getEventType() {
		return eventType;
	}

	public void setEventType(String eventType) {
		this.eventType = eventType;
	}

	public String getEventId() {
		return eventId;
	}

	public void setEventId(String eventId) {
		this.eventId = eventId;
	}

	public String getApplication() {
		return application;
	}

	public void setApplication(String application) {
		this.application = application;
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

	public String getReleaseVersion() {
		return releaseVersion;
	}

	public void setReleaseVersion(String releaseVersion) {
		this.releaseVersion = releaseVersion;
	}

	public LocalDateTime getReceivedTime() {
		return receivedTime;
	}

	public void setReceivedTime(LocalDateTime receivedTime) {
		this.receivedTime = receivedTime;
	}

	public String getFullPayload() {
		return fullPayload;
	}

	public void setFullPayload(String fullPayload) {
		this.fullPayload = fullPayload;
	}
    
    
}
