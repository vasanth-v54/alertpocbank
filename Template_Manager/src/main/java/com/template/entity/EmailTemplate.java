package com.template.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "DXP_EMAIL_TEMPLATE")
@Data
public class EmailTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "TEMPLATE_NAME")
    private String templateName;

    @Column(name = "TEMPLATE_BODY", columnDefinition = "json")
    private String templateBody;

    @Column(name = "IS_ACTIVE", nullable = false)
    private Boolean active;

    @Column(name = "DATE_CREATED", nullable = false)
    private LocalDateTime dateCreated;

    @Column(name = "DATE_UPDATED")
    private LocalDateTime dateUpdated;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (active == null) active = Boolean.TRUE;
        if (dateCreated == null) dateCreated = now;
        if (dateUpdated == null) dateUpdated = now;
    }

    @PreUpdate
    public void preUpdate() {
        dateUpdated = LocalDateTime.now();
    }
}