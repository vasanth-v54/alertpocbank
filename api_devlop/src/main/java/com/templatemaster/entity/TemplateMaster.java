package com.templatemaster.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "template_master",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_template",
                columnNames = {"template_name", "message_type", "is_active"}
        )
)
@Data
public class TemplateMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "template_name", nullable = false)
    private String templateName;

    @Column(name = "version")
    private String version;

    @Column(name = "message_type")
    private String messageType;

    @Column(name = "alert_config", columnDefinition = "json")
    private String alertConfig;

    @Column(name = "headers", columnDefinition = "json")
    private String headers;

    @Column(name = "raw_content", columnDefinition = "json")
    private String rawContent;

    @Column(name = "indexed_content", columnDefinition = "json")
    private String indexedContent;

    @Column(name = "param_mapping", columnDefinition = "json")
    private String paramMapping;

    @Column(name = "contentHash")
    private String contentHash;

    @Column(name = "exceptionReason")
    private String exceptionReason;

    @Column(name = "isDuplicateallowed")
    private Boolean duplicateAllowed;

    @Column(name = "is_active")
    private String isActive;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "modified_by")
    private String modifiedBy;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "modified_date")
    private LocalDateTime modifiedDate;

    @PreUpdate
    public void preUpdate() {
        modifiedDate = LocalDateTime.now();
        if (modifiedBy == null || modifiedBy.isBlank()) modifiedBy = "SYSTEM";
    }
    @PrePersist
    public void prePersist() {
        if (isActive == null || isActive.isBlank()) {
            isActive = "1";   // ✅ default
        }
    }
}