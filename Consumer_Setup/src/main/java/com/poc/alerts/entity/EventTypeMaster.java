package com.poc.alerts.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "event_type_master")
public class EventTypeMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="event_unique_id")
    private String eventUniqueId;

    @Column(name="eventType")
    private String eventType;

    @Column(name="alertType")
    private String alertType;

    @Column(name="originating_source")
    private String originatingSource;

    @Column(name="application")
    private String application;

    @Column(name="messageType")
    private String messageType;

    @Column(name="enabled")
    private Boolean enabled;

    public String getEventUniqueId() {
        return eventUniqueId;
    }

    public String getEventType() {
        return eventType;
    }

    public String getAlertType() {
        return alertType;
    }

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getOriginatingSource() {
		return originatingSource;
	}

	public void setOriginatingSource(String originatingSource) {
		this.originatingSource = originatingSource;
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

	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

	public void setEventUniqueId(String eventUniqueId) {
		this.eventUniqueId = eventUniqueId;
	}

	public void setEventType(String eventType) {
		this.eventType = eventType;
	}

	public void setAlertType(String alertType) {
		this.alertType = alertType;
	}
    
    
}