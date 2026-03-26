package com.notification.consumer.dto;

import lombok.Data;

@Data
public class EmailRequest {

    private String from;
    private String to;
    private String cc;
    private String bcc;
    private String subject;
    private String template;
	public String getFrom() {
		return from;
	}
	public void setFrom(String from) {
		this.from = from;
	}
	public String getTo() {
		return to;
	}
	public void setTo(String to) {
		this.to = to;
	}
	public String getCc() {
		return cc;
	}
	public void setCc(String cc) {
		this.cc = cc;
	}
	public String getBcc() {
		return bcc;
	}
	public void setBcc(String bcc) {
		this.bcc = bcc;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	
	public String getTemplate() {
		return template;
	}
	public void setTemplate(String template) {
		this.template = template;
	}
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("EmailRequest [from=");
		builder.append(from);
		builder.append(", to=");
		builder.append(to);
		builder.append(", cc=");
		builder.append(cc);
		builder.append(", bcc=");
		builder.append(bcc);
		builder.append(", subject=");
		builder.append(subject);
		builder.append(", template=");
		builder.append(template);
		builder.append("]");
		return builder.toString();
	}
    
    
}