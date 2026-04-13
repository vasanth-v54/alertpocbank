package com.template.service;

import com.template.dto.TemplateRequest;
import com.template.entity.EmailTemplate;
import com.template.entity.SmsTemplate;
import com.template.entity.TemplateMaster;
import com.template.repository.EmailTemplateRepository;
import com.template.repository.SmsTemplateRepository;
import com.template.repository.TemplateMasterRepository;
import com.template.util.JsonUtil;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class TemplateService1 {
    @Autowired
    private TemplateMasterRepository masterRepo;

    @Autowired
    private SmsTemplateRepository smsRepo;

    @Autowired
    private EmailTemplateRepository emailRepo;

    @Transactional
    public String createTemplate(TemplateRequest request) {

        validate(request);

        //  SAVE SINGLE MASTER RECORD
        TemplateMaster entity = new TemplateMaster();

        entity.setTemplateName(getTemplateName(request));
        entity.setMessageType(request.getMessageType()); // SMS / EMAIL / BOTH
        entity.setVersion(request.getVersion());

        entity.setAlertConfig(JsonUtil.toJson(request.getAlert_config()));
        entity.setHeaders(JsonUtil.toJson(request.getHeaders()));
        entity.setRawContent(JsonUtil.toJson(request.getRaw_content()));
        entity.setIndexedContent(JsonUtil.toJson(request.getIndexed_content()));
        entity.setParamMapping(JsonUtil.toJson(request.getParam_mapping()));

        entity.setContentHash(request.getContentHash());
        entity.setExceptionReason(request.getExceptionReason());
        entity.setDuplicateAllowed(request.getIsDuplicateallowed());

        entity.setIsActive("1");
        entity.setCreatedBy("SYSTEM");
        entity.setModifiedBy("SYSTEM");
        entity.setCreatedDate(LocalDateTime.now());
        entity.setModifiedDate(LocalDateTime.now());

        masterRepo.save(entity);

        // 🔥 STEP 2: CHANNEL INSERT
        Map<String, Object> indexed = request.getIndexed_content();

        String smsContent = (String) indexed.get("indexed_content_sms");
        String emailContent = (String) indexed.get("indexed_content_email");

        switch (request.getMessageType().toUpperCase()) {

            case "SMS" -> saveSms(request, smsContent);

            case "EMAIL" -> saveEmail(request, emailContent);

            case "BOTH" -> {
                saveSms(request, smsContent);
                saveEmail(request, emailContent);
            }
        }

        return "Template Created Successfully";
    }

    private String getTemplateName(TemplateRequest request) {
        if ("SMS".equalsIgnoreCase(request.getMessageType())) {
            return request.getTemplate_name().get("sms");
        } else if ("EMAIL".equalsIgnoreCase(request.getMessageType())) {
            return request.getTemplate_name().get("email");
        } else {
            return request.getTemplate_name().get("sms"); // common name
        }
    }

    private void saveSms(TemplateRequest request, String content) {
        if (content == null) return;

        SmsTemplate sms = new SmsTemplate();
        sms.setTemplateName(request.getTemplate_name().get("sms"));
        sms.setTemplateBody(JsonUtil.toJson(Map.of("indexed_content_sms", content)));
        sms.setActive(true);
        sms.setDateCreated(LocalDateTime.now());
        sms.setDateUpdated(LocalDateTime.now());

        smsRepo.save(sms);
    }

    private void saveEmail(TemplateRequest request, String content) {
        if (content == null) return;

        EmailTemplate email = new EmailTemplate();
        email.setTemplateName(request.getTemplate_name().get("email"));
        email.setTemplateBody(JsonUtil.toJson(Map.of("indexed_content_email", content)));
        email.setActive(true);
        email.setDateCreated(LocalDateTime.now());
        email.setDateUpdated(LocalDateTime.now());

        emailRepo.save(email);
    }

    private void validate(TemplateRequest request) {

        if (request.getMessageType() == null)
            throw new RuntimeException("messageType required");

        if ("BOTH".equalsIgnoreCase(request.getMessageType())) {
            if (request.getTemplate_name().get("sms") == null ||
                    request.getTemplate_name().get("email") == null) {
                throw new RuntimeException("Both SMS & Email names required");
            }
        }

        if (Boolean.TRUE.equals(request.getIsDuplicateallowed()) &&
                request.getExceptionReason() == null) {
            throw new RuntimeException("exceptionReason required when duplicate allowed");
        }
    }
}
