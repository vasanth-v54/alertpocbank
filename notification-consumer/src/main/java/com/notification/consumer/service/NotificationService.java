package com.notification.consumer.service;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.notification.consumer.dlt.DltService;
import com.notification.consumer.dto.NotificationRequest;
import com.notification.consumer.entity.TemplateMaster;
import com.notification.consumer.logger.VerticalLogger;
import com.notification.consumer.util.TemplateEngineUtil;

@Service
public class NotificationService {

	private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

	private final AppConfigService configService;

	@Autowired
	private TemplateMasterService templateMasterService;

	public NotificationService(AppConfigService configService, DltService dltService, VerticalLogger vlog,
			Map<String, String> emptyHeaders) {
		super();
		this.configService = configService;
		this.dltService = dltService;
		this.vlog = vlog;
		this.emptyHeaders = emptyHeaders;
	}

	private final ObjectMapper objectMapper = new ObjectMapper();
	private final DltService dltService;
	private final VerticalLogger vlog;
	Map<String, String> emptyHeaders = new HashMap<>();

	public void process(String payload, Map<String, String> headers) {

		String eventId = headers != null ? headers.getOrDefault("event-id", "N/A") : "N/A";

		try {
			ObjectMapper mapper = new ObjectMapper();

			// ================= STAGE 2 =================
			vlog.stageStart(3, "CONSUMER CORE", eventId);
			vlog.field("EVENT_ID", eventId);
			vlog.section("PAYLOAD RECEIVED :: KEY PARAMETERS VALIDATED");

			JsonNode root = mapper.readTree(payload);
			String eventType = root.path("eventType").asText(null);

			String customFieldDetails = extractCustomFieldDetails(payload);
			JsonNode customroot = mapper.readTree(customFieldDetails);

			String messageType = customroot.path("MessageType").asText(null);
			String alertType = customroot.path("alertType").asText(null);

			vlog.field("EVENT_TYPE", eventType);
			vlog.field("ALERT_TYPE", alertType);
			vlog.field("MESSAGE_TYPE", messageType);

			

			boolean preCheckStatus = false;

			boolean channelStatus = false;
			
			List<String> preCheckkeys = List.of("alertType", "eventType", "originatingSource");
			List<String> smsCheckList = List.of("mobileNumber");
			List<String> emailCheckList = List.of("to");

			Map<String, Boolean> preCheckresult = checkKeys(payload, preCheckkeys);
			Map<String, Boolean> smsCheckresult = checkKeys(payload, smsCheckList);
			Map<String, Boolean> emailCheckresult = checkKeys(payload, emailCheckList);

			System.out.println(preCheckresult);

			String preCheckMissing = preCheckresult.entrySet().stream().filter(entry -> !entry.getValue()).map(Map.Entry::getKey)
					.reduce((a, b) -> a + ", " + b).orElse("");
			String smsCheckMissing = smsCheckresult.entrySet().stream().filter(entry -> !entry.getValue()).map(Map.Entry::getKey)
					.reduce((a, b) -> a + ", " + b).orElse("");
			String emailCheckMissing = emailCheckresult.entrySet().stream().filter(entry -> !entry.getValue()).map(Map.Entry::getKey)
					.reduce((a, b) -> a + ", " + b).orElse("");

			if (!preCheckMissing.isEmpty()) {
				String error = "Missing required fields: " + preCheckMissing;
				log.error(error);
				vlog.field("ERROR_MESSAGE", error);
				vlog.field("EXECUTION_STOPPED", "Stage 3 — " + error);
				vlog.stageError(3, "REQUIRED FIELD VALIDATION", error, null, eventId);
				return;
			} else {
				preCheckStatus = true;
			}
			
			if (!smsCheckMissing.isEmpty() && (messageType.equalsIgnoreCase("SMS") || messageType.equalsIgnoreCase("BOTH"))) {
				String error = "Missing required fields for SMS: " + smsCheckMissing;
				log.error(error);
				vlog.field("ERROR_MESSAGE", error);
				vlog.field("EXECUTION_STOPPED", "Stage 3 — " + error);
				vlog.stageError(3, "REQUIRED FIELD VALIDATION", error, null, eventId);
				return;
			} else {
				channelStatus = true;
			}
			
			if (!emailCheckMissing.isEmpty() && (messageType.equalsIgnoreCase("EMAIL")|| messageType.equalsIgnoreCase("BOTH"))) {
				String error = "Missing required fields for EMAIL: " + emailCheckMissing;
				log.error(error);
				vlog.field("ERROR_MESSAGE", error);
				vlog.field("EXECUTION_STOPPED", "Stage 3 — " + error);
				vlog.stageError(3, "REQUIRED FIELD VALIDATION", error, null, eventId);
				return;
			} else {
				channelStatus = true;
			}
			
			if (preCheckStatus && channelStatus) {
				vlog.stageEnd(3, "CONSUMER CORE", "SUCCESS", eventId);
				
				List<TemplateMaster> templates = null;
				if (messageType.equalsIgnoreCase("BOTH")) {
					log.info("Check 1 for " + messageType);
					vlog.stageStart(4, "TEMPLATE MASTER VALIDATION", eventId);
					templates = templateMasterService.getAllActiveTemplates();
					log.info("Templates " + templates);
					// For Both Logic
					
					List<TemplateMaster> matchedTemplates = templates.stream()
						    .filter(t -> {
						        Map<String, Object> alertConfig = t.getAlertConfig();

						        if (alertConfig == null) return false;

						        String templateEventType = String.valueOf(alertConfig.get("eventType"));
						        String templateAlertType = String.valueOf(alertConfig.get("alertType"));

						        return eventType.equalsIgnoreCase(templateEventType)
						                && alertType.equalsIgnoreCase(templateAlertType);
						    })
						    .toList();

						if (matchedTemplates.isEmpty()) {
						    String error = "No templates found for eventType=" + eventType + ", alertType=" + alertType;
						    log.error(error);
						    dltService.logDlt(payload, headers, error);
						    return;
						}
						vlog.field("Fetched Templates for BOTH", matchedTemplates.toString());
						vlog.stageEnd(4, "TEMPLATE MASTER VALIDATION", "SUCCESS", eventId);
						// 🔥 Loop through matched templates
						for (TemplateMaster template : matchedTemplates) {

						    String type = template.getMessageType(); // SMS or EMAIL
						    NotificationRequest finalMessage = null;

						    if ("SMS".equalsIgnoreCase(type)) {

						        finalMessage = TemplateEngineUtil.buildMessage(
						                payload,
						                template.getIndexedContent(),
						                template.getParamMapping(),
						                "smsContent"
						        );

						        vlog.stageStart(5, "FINAL RESULT", eventId);
						        vlog.field("Final Result", "\n\n"+finalMessage.getKeyRequest()+"\n");
						        vlog.field("Final Result", "\n\n"+finalMessage.getResult()+"\n");
						        log.info("Final BOTH | SMS Message:\n{}", finalMessage);
						        vlog.stageEnd(5, "FINAL RESULT", "SUCCESS", eventId);

						        // 👉 send SMS

						    } else if ("EMAIL".equalsIgnoreCase(type)) {

						        finalMessage = TemplateEngineUtil.buildMessage(
						                payload,
						                template.getIndexedContent(),
						                template.getParamMapping(),
						                "emailContent"
						        );
						        
						        vlog.stageStart(5, "FINAL RESULT", eventId);
						        vlog.field("Final Result BOTH|EMAIL", "\n\n"+finalMessage+"\n");
						        log.info("Final BOTH | EMAIL Message:\n{}", finalMessage);
						        vlog.stageEnd(5, "FINAL RESULT", "SUCCESS", eventId);

						        // 👉 send EMAIL
						    }
						}

				} else {
					log.info("Check 2 for " + messageType);
					vlog.stageStart(4, "TEMPLATE MASTER VALIDATION", eventId);
					templates = templateMasterService.getActiveTemplatesByMessageType(messageType);
					log.info("Templates " + templates);
					
					TemplateMaster matchedTemplate = templates.stream().filter(t -> {
						Map<String, Object> alertConfig = t.getAlertConfig();

						if (alertConfig == null)
							return false;

						String templateEventType = (String) alertConfig.get("eventType");
						String templateAlertType = (String) alertConfig.get("alertType");

						return eventType.equalsIgnoreCase(templateEventType)
								&& alertType.equalsIgnoreCase(templateAlertType);
					}).findFirst().orElse(null);

					log.info("matchedTemplate " + templates);

					if (matchedTemplate == null) {
						String error = "No template found for eventType=" + eventType + ", alertType=" + alertType;

						log.error(error);
						dltService.logDlt(payload, headers, error);
						return;
					} else {
						vlog.field("Message Type", messageType);
						vlog.field("Fetched Templates", matchedTemplate.toString());
						vlog.stageEnd(4, "TEMPLATE MASTER VALIDATION", "SUCCESS", eventId);
						// write a logic
						NotificationRequest finalMessage = null;
						if (messageType.equalsIgnoreCase("SMS")) {
							finalMessage = TemplateEngineUtil.buildMessage(payload, matchedTemplate.getIndexedContent(),
									matchedTemplate.getParamMapping(), "smsContent" // or "smsContent"
							);
						} else if (messageType.equalsIgnoreCase("EMAIL")) {
							finalMessage = TemplateEngineUtil.buildMessage(payload, matchedTemplate.getIndexedContent(),
									matchedTemplate.getParamMapping(), "emailContent" // or "smsContent"
							);
						}
						
						vlog.stageStart(5, "FINAL RESULT", eventId);
				        log.info("Final BOTH | EMAIL Message:\n{}", finalMessage);
				        vlog.field("Final Result", "\n\n"+finalMessage.getKeyRequest()+"\n");
				        vlog.field("Final Result", "\n\n"+finalMessage.getResult()+"\n");
				        vlog.stageEnd(5, "FINAL RESULT", "SUCCESS", eventId);
					}
				}

			} else {
				String error = "Invalid Payload | Please check required Fields are Available or Not";
				dltService.logDlt(payload, headers, error);
				vlog.field("ERROR_MESSAGE", error);
				vlog.field("EXECUTION_STOPPED", "Stage 3 — " + error);
				vlog.stageError(3, "REQUIRED FIELD VALIDATION", error, null, eventId);
				return;
			}

		} catch (Exception e) {
			log.error("Exception", e);
			vlog.stageError(3, "CONSUMER CORE", e.getMessage(), null, eventId);
			dltService.logDlt(payload, headers, e.getMessage());
		}
	}

	public String extractCustomFieldDetails(String payload) throws Exception {
		JsonNode rootNode = objectMapper.readTree(payload);
		JsonNode customFieldDetails = rootNode.path("payload").path("customFieldDetails");
		return objectMapper.writeValueAsString(customFieldDetails);
	}

	public static boolean isNullOrEmpty(String value) {
		return value == null || value.trim().isEmpty() || value.equalsIgnoreCase("null");
	}

	private static final ObjectMapper mapper = new ObjectMapper();

	public static Map<String, Boolean> checkKeys(String payload, List<String> keys) {

		Map<String, Boolean> result = new HashMap<>();

		try {
			JsonNode root = mapper.readTree(payload);

			for (String key : keys) {
				boolean exists = findKey(root, key);
				result.put(key, exists);
			}

		} catch (Exception e) {
			throw new RuntimeException("Error checking keys", e);
		}

		return result;
	}

	// 🔥 Recursive search
	private static boolean findKey(JsonNode node, String targetKey) {

	    if (node == null) return false;

	    // 🔥 Case-insensitive check
	    if (node.isObject()) {
	        Iterator<String> fieldNames = node.fieldNames();

	        while (fieldNames.hasNext()) {
	            String field = fieldNames.next();

	            if (field.equalsIgnoreCase(targetKey)) {
	                return true;
	            }

	            if (findKey(node.get(field), targetKey)) {
	                return true;
	            }
	        }
	    }

	    if (node.isArray()) {
	        for (JsonNode element : node) {
	            if (findKey(element, targetKey)) return true;
	        }
	    }

	    return false;
	}

}