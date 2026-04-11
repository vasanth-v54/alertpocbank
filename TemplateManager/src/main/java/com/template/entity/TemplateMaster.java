package com.template.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "template_master")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TemplateMaster {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "template_name", nullable = false)
	private String templateName;

    // Corrected the field type to be the enum
    @Enumerated(EnumType.STRING)
	@Column(name = "messageType", nullable = false)
	private Channel messageType;

	@Column(name = "version", nullable = false)
	private String version;

	@Column(name = "alert_config", columnDefinition = "json", nullable = false)
	private String alertConfig;

	@Column(name = "headers", columnDefinition = "json", nullable = false)
	private String headers;

	@Column(name = "raw_content", columnDefinition = "json", nullable = false)
	private String rawContent;

	@Column(name = "indexed_content", columnDefinition = "json", nullable = false)
	private String indexedContent;

	@Column(name = "param_mapping", columnDefinition = "json", nullable = false)
	private String paramMapping;

	@Column(name = "contentHash")
	private String contentHash;

	@Column(name = "exceptionReason")
	private String exceptionReason;

	@Column(name = "isDuplicateallowed", nullable = false)
	private Boolean isDuplicateAllowed;

	@Column(name = "IS_ACTIVE", nullable = false)
	private Boolean isActive;

	@Column(name = "created_by", nullable = false)
	private String createdBy;

	@Column(name = "modified_by")
	private String modifiedBy;

	@Column(name = "created_date")
	private LocalDateTime createdDate;

	@Column(name = "modified_date")
	private LocalDateTime modifiedDate;

    public enum Channel {
        EMAIL, SMS, BOTH
    }

    // Removed the manual getters and setters for messageType to avoid conflict with Lombok
}