package com.notification.consumer.dto;

import lombok.Data;

@Data
public class SmsRequest {

    private String from;
    private String mobileNumber;
    private String message;
    private String template;
    private String callbackUrl;
    private String referenceId;
    private String notificationType;
	public String getFrom() {
		return from;
	}
	public void setFrom(String from) {
		this.from = from;
	}
	public String getMobileNumber() {
		return mobileNumber;
	}
	public void setMobileNumber(String mobileNumber) {
		this.mobileNumber = mobileNumber;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getTemplate() {
		return template;
	}
	public void setTemplate(String template) {
		this.template = template;
	}
	public String getCallbackUrl() {
		return callbackUrl;
	}
	public void setCallbackUrl(String callbackUrl) {
		this.callbackUrl = callbackUrl;
	}
	public String getReferenceId() {
		return referenceId;
	}
	public void setReferenceId(String referenceId) {
		this.referenceId = referenceId;
	}
	public String getNotificationType() {
		return notificationType;
	}
	public void setNotificationType(String notificationType) {
		this.notificationType = notificationType;
	}
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("SmsRequest [from=");
		builder.append(from);
		builder.append(", mobileNumber=");
		builder.append(mobileNumber);
		builder.append(", message=");
		builder.append(message);
		builder.append(", template=");
		builder.append(template);
		builder.append(", callbackUrl=");
		builder.append(callbackUrl);
		builder.append(", referenceId=");
		builder.append(referenceId);
		builder.append(", notificationType=");
		builder.append(notificationType);
		builder.append("]");
		return builder.toString();
	}
    
    
}