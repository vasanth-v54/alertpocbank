package com.consumer.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "EWB_API_REQ_RESP_MTB")
public class EwbApiReqRespMtb {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "API_ID")
    private String apiId;

    @Lob
    @Column(name = "REQUEST")
    private String request;

    @Lob
    @Column(name = "RESPONSE")
    private String response;

    @Column(name = "CREATED_BY")
    private String createdBy;

    @Column(name = "CREATED_ON")
    private LocalDateTime createdOn;

    @Column(name = "FLEXI_FIELD_1")
    private String flexiField1;

    @Column(name = "FLEXI_FIELD_2")
    private String flexiField2;

    @Column(name = "FLEXI_FIELD_3")
    private String flexiField3;

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

    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
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

    @Override
    public String toString() {
        return "EwbApiReqRespMtb [id=" + id + ", apiId=" + apiId + ", request=" + request + ", response=" + response
                + ", createdBy=" + createdBy + ", createdOn=" + createdOn + ", flexiField1=" + flexiField1
                + ", flexiField2=" + flexiField2 + ", flexiField3=" + flexiField3 + "]";
    }

    
}