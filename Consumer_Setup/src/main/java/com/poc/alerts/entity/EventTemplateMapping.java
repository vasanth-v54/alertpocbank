package com.poc.alerts.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "event_template_mapping")
public class EventTemplateMapping {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "mapping_id")
	private Long mappingId;

    @Column(name="event_unique_id")
    private String eventUniqueId;

    @Column(name="template_id")
    private Long templateId;

    @Column(name="alert_channel")
    private String alertChannel;

    @Column(name="template_params")
    private String templateParams;

    @Column(name="priority")
    private Integer priority;

    @Column(name="enabled")
    private Boolean enabled;

    public Long getTemplateId() {
        return templateId;
    }

    public String getEventUniqueId() {
        return eventUniqueId;
    }

    public String getAlertChannel() {
        return alertChannel;
    }

	public Long getMappingId() {
		return mappingId;
	}

	public void setMappingId(Long mappingId) {
		this.mappingId = mappingId;
	}

	public String getTemplateParams() {
		return templateParams;
	}

	public void setTemplateParams(String templateParams) {
		this.templateParams = templateParams;
	}

	public Integer getPriority() {
		return priority;
	}

	public void setPriority(Integer priority) {
		this.priority = priority;
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

	public void setTemplateId(Long templateId) {
		this.templateId = templateId;
	}

	public void setAlertChannel(String alertChannel) {
		this.alertChannel = alertChannel;
	}
    
    
}