package com.notification.consumer.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "routing_key_config")
public class RoutingKeyConfig {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String configName;

	@Column(columnDefinition = "json")
	private String templateIdentifiers; // store JSON as String

	private Boolean isActive;

	private LocalDateTime effectiveFrom;

	private LocalDateTime createDate;
	private String createdBy;

	private LocalDateTime updateDate;
	private String updatedBy;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getConfigName() {
		return configName;
	}

	public void setConfigName(String configName) {
		this.configName = configName;
	}

	public String getTemplateIdentifiers() {
		return templateIdentifiers;
	}

	public void setTemplateIdentifiers(String templateIdentifiers) {
		this.templateIdentifiers = templateIdentifiers;
	}

	public Boolean getIsActive() {
		return isActive;
	}

	public void setIsActive(Boolean isActive) {
		this.isActive = isActive;
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
		builder.append(", configName=");
		builder.append(configName);
		builder.append(", templateIdentifiers=");
		builder.append(templateIdentifiers);
		builder.append(", isActive=");
		builder.append(isActive);
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