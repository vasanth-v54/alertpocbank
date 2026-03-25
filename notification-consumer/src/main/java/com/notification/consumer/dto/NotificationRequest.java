package com.notification.consumer.dto;

import lombok.Data;
import java.util.Map;

@Data
public class NotificationRequest {

    private String type; // SMS / EMAIL

    private SmsRequest sms;
    private EmailRequest email;

    private Map<String, String> templateParams; // 🔥 dynamic

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public SmsRequest getSms() {
		return sms;
	}

	public void setSms(SmsRequest sms) {
		this.sms = sms;
	}

	public EmailRequest getEmail() {
		return email;
	}

	public void setEmail(EmailRequest email) {
		this.email = email;
	}

	public Map<String, String> getTemplateParams() {
		return templateParams;
	}

	public void setTemplateParams(Map<String, String> templateParams) {
		this.templateParams = templateParams;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("NotificationRequest [type=");
		builder.append(type);
		builder.append(", sms=");
		builder.append(sms);
		builder.append(", email=");
		builder.append(email);
		builder.append(", templateParams=");
		builder.append(templateParams);
		builder.append("]");
		return builder.toString();
	}
    
    
}