package com.poc.alerts.model;

import java.util.Map;

public class NotificationRequest {

    private String mobileNumber;
    private String emailId;
    private String templateId;
    private String referenceId; // 12 digit alphanumeric

    private Map<String, Object> templateData;

    public NotificationRequest() {}

    public NotificationRequest(String mobileNumber, String emailId, String templateId,
                               String referenceId, Map<String, Object> templateData) {
        this.mobileNumber = mobileNumber;
        this.emailId = emailId;
        this.templateId = templateId;
        this.referenceId = referenceId;
        this.templateData = templateData;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    public Map<String, Object> getTemplateData() {
        return templateData;
    }

    public void setTemplateData(Map<String, Object> templateData) {
        this.templateData = templateData;
    }

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("NotificationRequest [mobileNumber=");
		builder.append(mobileNumber);
		builder.append(", emailId=");
		builder.append(emailId);
		builder.append(", templateId=");
		builder.append(templateId);
		builder.append(", referenceId=");
		builder.append(referenceId);
		builder.append(", templateData=");
		builder.append(templateData);
		builder.append("]");
		return builder.toString();
	}
    
    
}