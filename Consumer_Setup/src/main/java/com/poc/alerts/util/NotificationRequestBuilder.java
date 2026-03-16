package com.poc.alerts.util;

import java.util.Map;

import com.poc.alerts.model.NotificationRequest;

public class NotificationRequestBuilder {

    public static NotificationRequest buildRequest(
            String mobileNumber,
            String emailId,
            String templateId,
            Map<String, Object> templateData) {

        NotificationRequest request = new NotificationRequest();

        request.setMobileNumber(mobileNumber);
        request.setEmailId(emailId);
        request.setTemplateId(templateId);
        request.setReferenceId(ReferenceGenerator.generateReferenceId());

        request.setTemplateData(templateData);

        return request;
    }
}