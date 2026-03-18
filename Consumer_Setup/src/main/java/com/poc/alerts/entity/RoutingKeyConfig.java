package com.poc.alerts.entity;

import java.time.LocalDateTime;
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
@Table(name = "routing_key_config")
public class RoutingKeyConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Type(JsonType.class)
    @Column(name = "template_identifiers", columnDefinition = "json", nullable = false)
    private Map<String, Object> templateIdentifiers;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "config_name", nullable = false)
    private String configName;

    @Column(name = "effective_from", nullable = false)
    private LocalDateTime effectiveFrom;

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

	public Map<String, Object> getTemplateIdentifiers() {
		return templateIdentifiers;
	}

	public void setTemplateIdentifiers(Map<String, Object> templateIdentifiers) {
		this.templateIdentifiers = templateIdentifiers;
	}

	public Boolean getIsActive() {
		return isActive;
	}

	public void setIsActive(Boolean isActive) {
		this.isActive = isActive;
	}

	public String getConfigName() {
		return configName;
	}

	public void setConfigName(String configName) {
		this.configName = configName;
	}

	public LocalDateTime getEffectiveFrom() {
		return effectiveFrom;
	}

	public void setEffectiveFrom(LocalDateTime effectiveFrom) {
		this.effectiveFrom = effectiveFrom;
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
		builder.append("RoutingKeyConfig [id=");
		builder.append(id);
		builder.append(", templateIdentifiers=");
		builder.append(templateIdentifiers);
		builder.append(", isActive=");
		builder.append(isActive);
		builder.append(", configName=");
		builder.append(configName);
		builder.append(", effectiveFrom=");
		builder.append(effectiveFrom);
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
