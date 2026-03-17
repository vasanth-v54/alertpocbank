package com.poc.alerts.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "payload_mst")
public class PayloadMst {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "payload_type")
    private String payloadType;

    @Column(name = "payload", columnDefinition = "TEXT")
    private String payload;

    @Column(name = "topic_status")
    private String topicStatus;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "created_by")
    private String createdBy;

    public PayloadMst() {
    }

    public PayloadMst(Long id,
                      String payloadType,
                      String payload,
                      String topicStatus,
                      Boolean isActive,
                      String createdBy) {

        this.id = id;
        this.payloadType = payloadType;
        this.payload = payload;
        this.topicStatus = topicStatus;
        this.isActive = isActive;
        this.createdBy = createdBy;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPayloadType() {
        return payloadType;
    }

    public void setPayloadType(String payloadType) {
        this.payloadType = payloadType;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String getTopicStatus() {
        return topicStatus;
    }

    public void setTopicStatus(String topicStatus) {
        this.topicStatus = topicStatus;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
}