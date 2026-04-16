package com.consumer.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "EWB_API_URL_MTB")
public class EwbApiUrlMtb {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "API_ID", unique = true, nullable = false)
    private String apiId;

    @Column(name = "URL")
    private String url;

    @Column(name = "TYPE")
    private String type;

    @Column(name = "CONNECTION_TIMEOUT")
    private Integer connectionTimeout;

    @Column(name = "READ_TIMEOUT")
    private Integer readTimeout;

    @Column(name = "AUDIT_FLAG")
    private Boolean auditFlag;

    @Column(name = "IS_ACTIVE")
    private Boolean isActive;

    @Column(name = "FLEXI_FIELD_1")
    private String flexiField1;

    @Column(name = "FLEXI_FIELD_2")
    private String flexiField2;

    @Column(name = "FLEXI_FIELD_3")
    private String flexiField3;

    @Column(name = "CREATED_BY")
    private String createdBy;

    @Column(name = "CREATED_ON")
    private LocalDateTime createdOn;

    @Column(name = "UPDATED_BY")
    private String updatedBy;

    @Column(name = "UPDATED_ON")
    private LocalDateTime updatedOn;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getApiId() {
        return apiId;
    }

    public void setApiId(String apiId) {
        this.apiId = apiId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(Integer connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public Integer getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(Integer readTimeout) {
        this.readTimeout = readTimeout;
    }

    public Boolean getAuditFlag() {
        return auditFlag;
    }

    public void setAuditFlag(Boolean auditFlag) {
        this.auditFlag = auditFlag;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public String getFlexiField1() {
        return flexiField1;
    }

    public void setFlexiField1(String flexiField1) {
        this.flexiField1 = flexiField1;
    }

    public String getFlexiField2() {
        return flexiField2;
    }

    public void setFlexiField2(String flexiField2) {
        this.flexiField2 = flexiField2;
    }

    public String getFlexiField3() {
        return flexiField3;
    }

    public void setFlexiField3(String flexiField3) {
        this.flexiField3 = flexiField3;
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

    @Override
    public String toString() {
        return "EwbApiUrlMtb [id=" + id + ", apiId=" + apiId + ", url=" + url + ", type=" + type
                + ", connectionTimeout=" + connectionTimeout + ", readTimeout=" + readTimeout + ", auditFlag="
                + auditFlag + ", isActive=" + isActive + ", flexiField1=" + flexiField1 + ", flexiField2=" + flexiField2
                + ", flexiField3=" + flexiField3 + ", createdBy=" + createdBy + ", createdOn=" + createdOn
                + ", updatedBy=" + updatedBy + ", updatedOn=" + updatedOn + ", getId()=" + getId() + ", getApiId()="
                + getApiId() + ", getUrl()=" + getUrl() + ", getType()=" + getType() + ", getConnectionTimeout()="
                + getConnectionTimeout() + ", getReadTimeout()=" + getReadTimeout() + ", getAuditFlag()="
                + getAuditFlag() + ", getIsActive()=" + getIsActive() + ", getFlexiField1()=" + getFlexiField1()
                + ", getFlexiField2()=" + getFlexiField2() + ", getFlexiField3()=" + getFlexiField3()
                + ", getCreatedBy()=" + getCreatedBy() + ", getCreatedOn()=" + getCreatedOn() + ", getUpdatedBy()="
                + getUpdatedBy() + ", getUpdatedOn()=" + getUpdatedOn() + ", getClass()=" + getClass() + ", hashCode()="
                + hashCode() + ", toString()=" + super.toString() + "]";
    }

   
    
}