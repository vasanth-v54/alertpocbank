package com.producer.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "payload_mst")
@Data
public class PayloadMst {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="payload_type")
    private String payloadType;

    @Column(columnDefinition="CLOB")
    private String payload;

    @Column(name="topic_status")
    private String topicStatus;

    @Column(name="is_active")
    private Boolean isActive;

    @Column(name="created_on", insertable=false, updatable=false)
    private LocalDateTime createdOn;

    public Long getId() { return id; }

    public String getPayloadType() { return payloadType; }

    public void setPayloadType(String payloadType) { this.payloadType = payloadType; }

    public String getPayload() { return payload; }

    public void setPayload(String payload) { this.payload = payload; }

    public String getTopicStatus() { return topicStatus; }

    public void setTopicStatus(String topicStatus) { this.topicStatus = topicStatus; }

    public Boolean getIsActive() { return isActive; }

    public void setIsActive(Boolean isActive) { this.isActive = isActive; }}