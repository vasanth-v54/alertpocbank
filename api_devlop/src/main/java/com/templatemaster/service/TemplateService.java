package com.templatemaster.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.templatemaster.dto.ApiResponse;
import com.templatemaster.dto.TemplateCreateData;
import com.templatemaster.dto.TemplateCreateRequest;
import com.templatemaster.entity.EmailTemplate;
import com.templatemaster.entity.SmsTemplate;
import com.templatemaster.entity.TemplateMaster;
import com.templatemaster.enums.MessageType;
import com.templatemaster.repository.EmailTemplateRepository;
import com.templatemaster.repository.SmsTemplateRepository;
import com.templatemaster.repository.TemplateMasterRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TemplateService {

    private final TemplateMasterRepository templateMasterRepository;
    private final SmsTemplateRepository smsTemplateRepository;
    private final EmailTemplateRepository emailTemplateRepository;
    private final ObjectMapper objectMapper;

    @Transactional(rollbackOn = Exception.class)
    public ApiResponse<TemplateCreateData> createTemplate(TemplateCreateRequest request) throws JsonProcessingException {
        validateRequest(request);
        validateDuplicatesIfNeeded(request);

        List<String> createdTemplateNames = new ArrayList<>();

        switch (request.getMessageType()) {
            case SMS -> {
                persistSmsFlow(request);
                createdTemplateNames.add(request.getTemplateName().getSms());
            }
            case EMAIL -> {
                persistEmailFlow(request);
                createdTemplateNames.add(request.getTemplateName().getEmail());
            }
            case BOTH -> {
                persistSmsFlow(request);
                persistEmailFlow(request);
                createdTemplateNames.add(request.getTemplateName().getSms());
                createdTemplateNames.add(request.getTemplateName().getEmail());
            }
        }

        return new ApiResponse<>(
                true,
                "Success",
                new TemplateCreateData(
                        request.getMessageType().name(),
                        request.getVersion(),
                        createdTemplateNames
                )
        );
    }

    private void persistSmsFlow(TemplateCreateRequest request) throws JsonProcessingException {
        String templateName = request.getTemplateName().getSms();
        String rawContent = request.getRawContent().getRawcontentSms();
        String indexedContent = request.getIndexedContent().getIndexedContentSms();

        String masterHeadersJson = buildJsonWithSingleKey("headers_sms", request.getHeaders().getHeadersSms());
        String masterRawJson = buildJsonWithSingleKey("rawcontent_sms", rawContent);
        String masterIndexedJson = buildJsonWithSingleKey("indexed_content_sms", indexedContent);
        String masterParamJson = buildJsonWithSingleKey("param_mapping_sms", request.getParamMapping().getParamMappingSms());

        saveTemplateMaster(
                request,
                templateName,
                MessageType.SMS.name(),
                masterHeadersJson,
                masterRawJson,
                masterIndexedJson,
                masterParamJson,
                indexedContent
        );

        SmsTemplate smsTemplate = new SmsTemplate();
        smsTemplate.setTemplateName(templateName);
        smsTemplate.setTemplateBody(buildJsonWithSingleKey("indexed_content_sms", indexedContent));
        smsTemplateRepository.save(smsTemplate);
    }

    private void persistEmailFlow(TemplateCreateRequest request) throws JsonProcessingException {
        String templateName = request.getTemplateName().getEmail();
        String rawContent = request.getRawContent().getRawcontentEmail();
        String indexedContent = request.getIndexedContent().getIndexedContentEmail();

        String masterHeadersJson = buildJsonWithSingleKey("headers_email", request.getHeaders().getHeadersEmail());
        String masterRawJson = buildJsonWithSingleKey("rawcontent_email", rawContent);
        String masterIndexedJson = buildJsonWithSingleKey("indexed_content_email", indexedContent);
        String masterParamJson = buildJsonWithSingleKey("param_mapping_email", request.getParamMapping().getParamMappingEmail());

        saveTemplateMaster(
                request,
                templateName,
                MessageType.EMAIL.name(),
                masterHeadersJson,
                masterRawJson,
                masterIndexedJson,
                masterParamJson,
                indexedContent
        );

        EmailTemplate emailTemplate = new EmailTemplate();
        emailTemplate.setTemplateName(templateName);
        emailTemplate.setTemplateBody(buildJsonWithSingleKey("indexed_content_email", indexedContent));
        emailTemplateRepository.save(emailTemplate);
    }

    private void saveTemplateMaster(
            TemplateCreateRequest request,
            String templateName,
            String channel,
            String headersJson,
            String rawContentJson,
            String indexedContentJson,
            String paramMappingJson,
            String indexedContentForHash
    ) throws JsonProcessingException {
        TemplateMaster master = new TemplateMaster();
        master.setTemplateName(templateName);
        master.setVersion(request.getVersion());
        master.setMessageType(channel);
        master.setAlertConfig(toJson(request.getAlertConfig()));
        master.setHeaders(headersJson);
        master.setRawContent(rawContentJson);
        master.setIndexedContent(indexedContentJson);
        master.setParamMapping(paramMappingJson);
        master.setContentHash(resolveContentHash(request.getContentHash(), indexedContentForHash));
        master.setExceptionReason(request.getExceptionReason());
        master.setDuplicateAllowed(Boolean.TRUE.equals(request.getDuplicateAllowed()));
        master.setIsActive("1");
        templateMasterRepository.save(master);
    }

    private void validateDuplicatesIfNeeded(TemplateCreateRequest request) {
        if (Boolean.TRUE.equals(request.getDuplicateAllowed())) {
            if (!StringUtils.hasText(request.getExceptionReason())) {
                throw new IllegalArgumentException("exceptionReason is required when isDuplicateallowed = true");
            }
            return;
        }

        if (request.getMessageType() == MessageType.SMS || request.getMessageType() == MessageType.BOTH) {
            String smsName = request.getTemplateName().getSms();
            if (templateMasterRepository.existsByTemplateNameAndMessageTypeAndIsActive(smsName, MessageType.SMS.name(), "1")) {
                throw new IllegalArgumentException("SMS template already exists in template_master");
            }
            if (smsTemplateRepository.existsByTemplateName(smsName)) {
                throw new IllegalArgumentException("SMS template already exists in DXP_SMS_TEMPLATE");
            }
        }

        if (request.getMessageType() == MessageType.EMAIL || request.getMessageType() == MessageType.BOTH) {
            String emailName = request.getTemplateName().getEmail();
            if (templateMasterRepository.existsByTemplateNameAndMessageTypeAndIsActive(emailName, MessageType.EMAIL.name(), "1")) {
                throw new IllegalArgumentException("Email template already exists in template_master");
            }
            if (emailTemplateRepository.existsByTemplateName(emailName)) {
                throw new IllegalArgumentException("Email template already exists in DXP_EMAIL_TEMPLATE");
            }
        }
    }

    private void validateRequest(TemplateCreateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required");
        }
        if (request.getMessageType() == null) {
            throw new IllegalArgumentException("messageType is required");
        }
        if (!StringUtils.hasText(request.getVersion())) {
            throw new IllegalArgumentException("version is required");
        }
        if (request.getTemplateName() == null) {
            throw new IllegalArgumentException("template_name is required");
        }
        if (request.getAlertConfig() == null) {
            throw new IllegalArgumentException("alert_config is required");
        }
        if (request.getHeaders() == null) {
            throw new IllegalArgumentException("headers is required");
        }
        if (request.getRawContent() == null) {
            throw new IllegalArgumentException("raw_content is required");
        }
        if (request.getIndexedContent() == null) {
            throw new IllegalArgumentException("indexed_content is required");
        }
        if (request.getParamMapping() == null) {
            throw new IllegalArgumentException("param_mapping is required");
        }

        switch (request.getMessageType()) {
            case SMS -> {
                if (!StringUtils.hasText(request.getTemplateName().getSms())) {
                    throw new IllegalArgumentException("template_name.sms is required for SMS");
                }
                if (!StringUtils.hasText(request.getRawContent().getRawcontentSms())) {
                    throw new IllegalArgumentException("raw_content.rawcontent_sms is required for SMS");
                }
                if (!StringUtils.hasText(request.getIndexedContent().getIndexedContentSms())) {
                    throw new IllegalArgumentException("indexed_content.indexed_content_sms is required for SMS");
                }
                if (request.getHeaders().getHeadersSms() == null) {
                    throw new IllegalArgumentException("headers.headers_sms is required for SMS");
                }
                if (request.getParamMapping().getParamMappingSms() == null) {
                    throw new IllegalArgumentException("param_mapping.param_mapping_sms is required for SMS");
                }
            }
            case EMAIL -> {
                if (!StringUtils.hasText(request.getTemplateName().getEmail())) {
                    throw new IllegalArgumentException("template_name.email is required for EMAIL");
                }
                if (!StringUtils.hasText(request.getRawContent().getRawcontentEmail())) {
                    throw new IllegalArgumentException("raw_content.rawcontent_email is required for EMAIL");
                }
                if (!StringUtils.hasText(request.getIndexedContent().getIndexedContentEmail())) {
                    throw new IllegalArgumentException("indexed_content.indexed_content_email is required for EMAIL");
                }
                if (request.getHeaders().getHeadersEmail() == null) {
                    throw new IllegalArgumentException("headers.headers_email is required for EMAIL");
                }
                if (request.getParamMapping().getParamMappingEmail() == null) {
                    throw new IllegalArgumentException("param_mapping.param_mapping_email is required for EMAIL");
                }
            }
            case BOTH -> {
                if (!StringUtils.hasText(request.getTemplateName().getSms())) {
                    throw new IllegalArgumentException("template_name.sms is required for BOTH");
                }
                if (!StringUtils.hasText(request.getTemplateName().getEmail())) {
                    throw new IllegalArgumentException("template_name.email is required for BOTH");
                }
                if (!StringUtils.hasText(request.getRawContent().getRawcontentSms())) {
                    throw new IllegalArgumentException("raw_content.rawcontent_sms is required for BOTH");
                }
                if (!StringUtils.hasText(request.getRawContent().getRawcontentEmail())) {
                    throw new IllegalArgumentException("raw_content.rawcontent_email is required for BOTH");
                }
                if (!StringUtils.hasText(request.getIndexedContent().getIndexedContentSms())) {
                    throw new IllegalArgumentException("indexed_content.indexed_content_sms is required for BOTH");
                }
                if (!StringUtils.hasText(request.getIndexedContent().getIndexedContentEmail())) {
                    throw new IllegalArgumentException("indexed_content.indexed_content_email is required for BOTH");
                }
                if (request.getHeaders().getHeadersSms() == null) {
                    throw new IllegalArgumentException("headers.headers_sms is required for BOTH");
                }
                if (request.getHeaders().getHeadersEmail() == null) {
                    throw new IllegalArgumentException("headers.headers_email is required for BOTH");
                }
                if (request.getParamMapping().getParamMappingSms() == null) {
                    throw new IllegalArgumentException("param_mapping.param_mapping_sms is required for BOTH");
                }
                if (request.getParamMapping().getParamMappingEmail() == null) {
                    throw new IllegalArgumentException("param_mapping.param_mapping_email is required for BOTH");
                }
            }
        }
    }

    private String buildJsonWithSingleKey(String key, Object value) throws JsonProcessingException {
        ObjectNode node = objectMapper.createObjectNode();
        node.set(key, objectMapper.valueToTree(value));
        return objectMapper.writeValueAsString(node);
    }

    private String toJson(Object value) throws JsonProcessingException {
        return objectMapper.writeValueAsString(value);
    }

    private String resolveContentHash(String requestContentHash, String indexedContent) {
        if (StringUtils.hasText(requestContentHash)) {
            return requestContentHash.trim();
        }
        return sha256(normalize(indexedContent));
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().toLowerCase().replaceAll("\\s+", " ");
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                String h = Integer.toHexString(0xff & b);
                if (h.length() == 1) {
                    hex.append('0');
                }
                hex.append(h);
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Unable to compute SHA-256", e);
        }
    }
}