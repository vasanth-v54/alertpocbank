package com.notification.consumer.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "app_config")
public class AppConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "config_key", unique = true, nullable = false)
    private String configKey;

    @Column(name = "config_value", nullable = false)
    private String configValue;

    @Column(name = "is_active")   // ✅ FIXED
    private Boolean isActive;

    @Column(name = "flexifield1")
    private String flexifield1;

    @Column(name = "flexifield2")
    private String flexifield2;

    @Column(name = "flexifield3")
    private String flexifield3;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "created_on")
    private LocalDateTime createdOn;

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "updated_on")
    private LocalDateTime updatedOn;

    // ================= GETTERS & SETTERS =================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getConfigKey() {
        return configKey;
    }

    public void setConfigKey(String configKey) {
        this.configKey = configKey;
    }

    public String getConfigValue() {
        return configValue;
    }

    public void setConfigValue(String configValue) {
        this.configValue = configValue;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public String getFlexifield1() {
        return flexifield1;
    }

    public void setFlexifield1(String flexifield1) {
        this.flexifield1 = flexifield1;
    }

    public String getFlexifield2() {
        return flexifield2;
    }

    public void setFlexifield2(String flexifield2) {
        this.flexifield2 = flexifield2;
    }

    public String getFlexifield3() {
        return flexifield3;
    }

    public void setFlexifield3(String flexifield3) {
        this.flexifield3 = flexifield3;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(LocalDateTime createdOn) {
        this.createdOn = createdOn;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public LocalDateTime getUpdatedOn() {
        return updatedOn;
    }

    public void setUpdatedOn(LocalDateTime updatedOn) {
        this.updatedOn = updatedOn;
    }
    
    
}