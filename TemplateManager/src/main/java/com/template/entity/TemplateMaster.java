package com.template.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "template_master")
@Data
public class TemplateMaster {

    @Id
    private Long id;

    @Column(name = "template_name")
    private String templateName;

    private String version;

    @Column(name = "message_type")
    private String messageType;

    @Column(columnDefinition = "json")
    private String alertConfig;

    @Column(columnDefinition = "json")
    private String headers;

    @Column(name = "raw_content", columnDefinition = "json")
    private String rawContent;

    @Column(name = "indexed_content", columnDefinition = "json")
    private String indexedContent;

    @Column(name = "param_mapping", columnDefinition = "json")
    private String paramMapping;

    private String contentHash;
    private String exceptionReason;

    private boolean isDuplicateallowed;

    @Column(name = "is_active")
    private String isActive;
}
