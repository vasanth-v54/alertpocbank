package com.poc.alerts.service;

import java.time.LocalDateTime;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.poc.alerts.entity.EventTemplateMapping;
import com.poc.alerts.entity.EventTypeMaster;
import com.poc.alerts.entity.ProcessedAlertAudit;
import com.poc.alerts.entity.TemplateMst;
import com.poc.alerts.repository.EventTemplateMappingRepository;
import com.poc.alerts.repository.EventTypeMasterRepository;
import com.poc.alerts.repository.ProcessedAlertAuditRepository;
import com.poc.alerts.repository.TemplateRepository;
import com.poc.alerts.util.PayloadParser;

@Service
public class TemplateProcessorService {

	private static final Logger templateLog = LoggerFactory.getLogger("TEMPLATE_PROCESS_LOGGER");

	@Autowired
	private TemplateRepository templateRepository;
	
	@Autowired
	private SmsService smsService;

	@Autowired
	private EventTypeMasterRepository eventTypeRepository;

	@Autowired
	private EventTemplateMappingRepository eventTemplateMappingRepository;

	@Autowired
	private ProcessedAlertAuditRepository processedAlertAuditRepository;
	
	public String processTemplate(String payload, String eventType, String alertType, String messageType)
			throws Exception {

		templateLog.info("Fetching event_unique_id from EVENT_TYPE_MASTER");

		EventTypeMaster eventTypeMaster =
		        eventTypeRepository
		        .findByEventTypeAndAlertTypeAndEnabledTrue(
		                eventType,
		                alertType)
		        .orElseThrow(() ->
		                new RuntimeException("EventType mapping not found"));

		String eventUniqueId =
		        eventTypeMaster.getEventUniqueId();

		templateLog.info("Event Unique Id : {}", eventUniqueId);


		templateLog.info("Fetching template mapping from EVENT_TEMPLATE_MAPPING");

		EventTemplateMapping mapping =
		        eventTemplateMappingRepository
		        .findFirstByEventUniqueIdAndAlertChannelAndEnabledTrueOrderByPriorityAsc(
		                eventUniqueId,
		                messageType)
		        .orElseThrow(() ->
		                new RuntimeException("Template mapping not found"));

		Long templateId = mapping.getTemplateId();

		templateLog.info("Template Id found : {}", templateId);


		templateLog.info("Fetching template from TEMPLATE_MST");
		templateLog.info("------------------------------------------------");
		templateLog.info("TEMPLATE PROCESS STARTED");
		templateLog.info("EventType: {}", eventType);
		templateLog.info("AlertType: {}", alertType);
		templateLog.info("MessageType: {}", messageType);

		templateLog.info("Fetching template from TEMPLATE_MST");

		TemplateMst template =
		        templateRepository
		            .findByAlertTypeAndMessageTypeAndTemplateStatus(
		                    alertType,
		                    messageType,
		                    "Y")
		            .orElse(null);

		if (template == null) {

			templateLog.error("No active template found in TEMPLATE_MST");
			return null;
		}

		templateLog.info("Template Found");
		templateLog.info("Template ID: {}", template.getTemplateId());
		templateLog.info("Template Subject: {}", template.getSubject());

		templateLog.info("Original Template:");
		templateLog.info(template.getTemplate());

		templateLog.info("Extracting required fields from payload");

		Map<String, String> values = PayloadParser.extractFieldsBasedOnAlertType(payload, alertType);

		templateLog.info("Extracted Payload Values: {}", values);

		templateLog.info("Replacing placeholders in template");

		String result = template.getTemplate();

		for (String key : values.keySet()) {

			templateLog.info("Replacing placeholder {} with value {}", key, values.get(key));

			result = result.replace("{" + key + "}", values.get(key));
		}

		templateLog.info("Final Generated Template:");
		templateLog.info(result);
		
		String mobileNumber = "919600525114";
		if("SMS".equalsIgnoreCase(messageType)){

		    templateLog.info("Sending SMS to mobile number : {}", mobileNumber);

		    smsService.sendSms(mobileNumber, result);

		    templateLog.info("SMS sending triggered");

		}

		ProcessedAlertAudit audit = new ProcessedAlertAudit();
		String eventId = PayloadParser.extractEventId(payload);
		audit.setMessageType(messageType);
		audit.setMessage(result);
		audit.setEventId(eventId);
		audit.setAlertType(alertType);
		audit.setCreatedOn(LocalDateTime.now());
		audit.setCreatedBy("SYSTEM");
		processedAlertAuditRepository.save(audit);

		templateLog.info("Processed alert stored in PROCESSED_ALERT_AUDIT table");
		templateLog.info("TEMPLATE PROCESS COMPLETED");
		templateLog.info("------------------------------------------------");

		return result;
	}
	
	
}