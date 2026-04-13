package com.template.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.template.dto.*;
import com.template.entity.EmailTemplate;
import com.template.entity.SmsTemplate;
import com.template.entity.TemplateMaster;
import com.template.enums.MessageType;
import com.template.repository.EmailTemplateRepository;
import com.template.repository.SmsTemplateRepository;
import com.template.repository.TemplateMasterRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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


    public TemplateDetailResponseDTO getTemplate(String templateName) {

        List<TemplateMaster> list =
                templateMasterRepository.findByTemplateNameAndIsActive(templateName, "1");

        if (list == null || list.isEmpty()) {
            throw new RuntimeException("Template not found");
        }


        boolean hasBoth = list.stream()
                .anyMatch(t -> "BOTH".equalsIgnoreCase(t.getMessageType()));


        if (hasBoth) {
            return mapBothResponse(list);
        }

        TemplateMaster latest = list.stream()
                .max(Comparator.comparing(this::getTime))
                .orElseThrow(() -> new RuntimeException("No latest template found"));

        return mapSingleResponse(latest);
    }


    //SINGLE RESPONSE
    private TemplateDetailResponseDTO mapSingleResponse(TemplateMaster entity) {

        TemplateDetailResponseDTO res = new TemplateDetailResponseDTO();

        try {

            String type = safeType(entity.getMessageType());

            Map<String, Object> headers = parseJson(entity.getHeaders());
            Map<String, Object> rawContent = parseJson(entity.getRawContent());
            Map<String, Object> indexedContent = parseJson(entity.getIndexedContent());
            Map<String, Object> paramMapping = parseJson(entity.getParamMapping());

            res.setId(entity.getId());
            res.setTemplate_name(entity.getTemplateName());
            res.setMessageType(entity.getMessageType());
            res.setVersion(entity.getVersion());
            res.setAlert_config(parseJson(entity.getAlertConfig()));

            Map<String, Object> headerMap = new HashMap<>();
            headerMap.put("headers_" + type,
                    headers != null ? getSafe(headers, type, "headers_" + type) : null);
            res.setHeaders(headerMap);

            Map<String, Object> rawMap = new HashMap<>();
            rawMap.put("rawcontent_" + type,
                    rawContent != null ? getContent(rawContent, type, "raw") : null);
            res.setRaw_content(rawMap);

            Map<String, Object> indexMap = new HashMap<>();
            indexMap.put("indexed_content_" + type,
                    indexedContent != null ? getContent(indexedContent, type, "indexed") : null);
            res.setIndexed_content(indexMap);

            Map<String, Object> paramMapFinal = new HashMap<>();
            paramMapFinal.put("param_mapping_" + type, mapParams(paramMapping));
            res.setParam_mapping(paramMapFinal);

            res.setContentHash(entity.getContentHash());
            res.setExceptionReason(entity.getExceptionReason());
            res.setIsDuplicateallowed(entity.getDuplicateAllowed());

            return res;

        } catch (Exception e) {
            throw new RuntimeException("Mapping error: " + e.getMessage(), e);
        }
    }


    //BOTH RESPONSE

    private TemplateDetailResponseDTO mapBothResponse(List<TemplateMaster> list) {

        TemplateDetailResponseDTO res = new TemplateDetailResponseDTO();

        //Pick latest per channel
        TemplateMaster latestSms = list.stream()
                .filter(t -> containsKey(t.getHeaders(), "headers_sms"))
                .max(Comparator.comparing(this::getTime))
                .orElse(null);

        TemplateMaster latestEmail = list.stream()
                .filter(t -> containsKey(t.getHeaders(), "headers_email"))
                .max(Comparator.comparing(this::getTime))
                .orElse(null);

        res.setTemplate_name(list.get(0).getTemplateName());
        res.setMessageType("BOTH");
        res.setVersion(list.get(0).getVersion());

        // ================= ID (JSON) =================
        Map<String, Object> idMap = new HashMap<>();
        if (latestSms != null) idMap.put("sms", latestSms.getId());
        if (latestEmail != null) idMap.put("email", latestEmail.getId());
        res.setId(idMap);

        // ================= ALERT CONFIG =================
        Map<String, Object> alertMap = new HashMap<>();
        if (latestSms != null) alertMap.put("sms", parseJson(latestSms.getAlertConfig()));
        if (latestEmail != null) alertMap.put("email", parseJson(latestEmail.getAlertConfig()));
        res.setAlert_config(alertMap);

        // ================= HEADERS =================
        Map<String, Object> headerMap = new HashMap<>();
        if (latestSms != null) {
            headerMap.put("sms",
                    getSafe(parseJson(latestSms.getHeaders()), "sms", "headers_sms"));
        }
        if (latestEmail != null) {
            headerMap.put("email",
                    getSafe(parseJson(latestEmail.getHeaders()), "email", "headers_email"));
        }
        res.setHeaders(headerMap);

        // ================= RAW =================
        Map<String, Object> rawMap = new HashMap<>();
        if (latestSms != null) {
            rawMap.put("sms",
                    getContent(parseJson(latestSms.getRawContent()), "sms", "raw"));
        }
        if (latestEmail != null) {
            rawMap.put("email",
                    getContent(parseJson(latestEmail.getRawContent()), "email", "raw"));
        }
        res.setRaw_content(rawMap);

        // ================= INDEXED =================
        Map<String, Object> indexMap = new HashMap<>();
        if (latestSms != null) {
            indexMap.put("sms",
                    getContent(parseJson(latestSms.getIndexedContent()), "sms", "indexed"));
        }
        if (latestEmail != null) {
            indexMap.put("email",
                    getContent(parseJson(latestEmail.getIndexedContent()), "email", "indexed"));
        }
        res.setIndexed_content(indexMap);

        // ================= PARAM =================
        Map<String, Object> paramMapFinal = new HashMap<>();
        if (latestSms != null) {
            paramMapFinal.put("sms",
                    mapParams(parseJson(latestSms.getParamMapping())));
        }
        if (latestEmail != null) {
            paramMapFinal.put("email",
                    mapParams(parseJson(latestEmail.getParamMapping())));
        }
        res.setParam_mapping(paramMapFinal);

        return res;
    }


    //HELPERS
    private boolean containsKey(String json, String key) {
        return json != null && json.contains(key);
    }

    private String safeType(String type) {
        return type != null ? type.toLowerCase() : "";
    }

    private LocalDateTime getTime(TemplateMaster t) {
        return t.getModifiedDate() != null
                ? t.getModifiedDate()
                : t.getCreatedDate();
    }

    private Map<String, Object> parseJson(String json) {
        try {
            if (json == null) return null;
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (Exception e) {
            return null;
        }
    }

    private Object getSafe(Map<String, Object> map, String key1, String key2) {
        if (map == null) return null;
        Object val = map.get(key1);
        if (val == null) val = map.get(key2);
        return val;
    }

    private Object getContent(Map<String, Object> map, String type, String contentType) {

        if (map == null) return null;

        if ("sms".equals(type)) {
            if ("raw".equals(contentType)) {
                return map.getOrDefault("smsContent", map.get("rawcontent_sms"));
            }
            if ("indexed".equals(contentType)) {
                return map.getOrDefault("smsContent", map.get("indexed_content_sms"));
            }
        } else {
            if ("raw".equals(contentType)) {
                return map.getOrDefault("emailContent", map.get("rawcontent_email"));
            }
            if ("indexed".equals(contentType)) {
                return map.getOrDefault("emailContent", map.get("indexed_content_email"));
            }
        }

        return null;
    }

    private List<Map<String, Object>> mapParams(Map<String, Object> paramMap) {

        List<Map<String, Object>> list = new ArrayList<>();
        if (paramMap == null) return list;

        boolean isOldFormat = paramMap.keySet().stream().allMatch(k -> k.matches("\\d+"));

        if (isOldFormat) {
            paramMap.forEach((k, v) -> {
                try {
                    Map<String, Object> obj = new HashMap<>();
                    obj.put("seq", Integer.parseInt(k));
                    obj.put("parameter", v);
                    obj.put("mappingType", "DIRECT");
                    obj.put("augExpression", null);
                    list.add(obj);
                } catch (Exception ignored) {
                }
            });
        } else {
            for (Object value : paramMap.values()) {
                if (value instanceof List<?>) {
                    for (Object item : (List<?>) value) {
                        if (item instanceof Map<?, ?> m) {
                            list.add(new HashMap<>((Map<String, Object>) m));
                        }
                    }
                }
            }
        }

        list.sort(Comparator.comparingInt(o -> (int) o.get("seq")));
        return list;
    }
    

    private FetchAllTemplatesResponseDTO mapBothSummary(List<TemplateMaster> list) {

        TemplateMaster latestSms = list.stream()
                .filter(t -> containsKey(t.getHeaders(), "headers_sms"))
                .max(Comparator.comparing(this::getTime))
                .orElse(null);

        TemplateMaster latestEmail = list.stream()
                .filter(t -> containsKey(t.getHeaders(), "headers_email"))
                .max(Comparator.comparing(this::getTime))
                .orElse(null);

        FetchAllTemplatesResponseDTO dto = new FetchAllTemplatesResponseDTO();

        dto.setTemplateName(list.get(0).getTemplateName());
        dto.setMsgType("BOTH");

        Map<String, Object> alert = parseJson(
                latestSms != null ? latestSms.getAlertConfig() : latestEmail.getAlertConfig()
        );

        if (alert != null) {
            dto.setDomain((String) alert.get("domain"));
            dto.setAlertName((String) alert.get("alertName"));
            dto.setAlertType((String) alert.get("alertType"));
            dto.setEventType((String) alert.get("eventType"));
        }

        if (latestSms != null) {
            dto.setSmsTemplate(
                    (String) getContent(parseJson(latestSms.getRawContent()), "sms", "raw")
            );
        }

        if (latestEmail != null) {
            dto.setEmailTemplate(
                    (String) getContent(parseJson(latestEmail.getRawContent()), "email", "raw")
            );
        }

        return dto;
    }

    private FetchAllTemplatesResponseDTO mapSingleSummary(List<TemplateMaster> list) {

        TemplateMaster latest = list.stream()
                .max(Comparator.comparing(this::getTime))
                .orElseThrow();

        FetchAllTemplatesResponseDTO dto = new FetchAllTemplatesResponseDTO();

        dto.setTemplateName(latest.getTemplateName());
        dto.setMsgType(latest.getMessageType());

        Map<String, Object> alert = parseJson(latest.getAlertConfig());

        if (alert != null) {
            dto.setDomain((String) alert.get("domain"));
            dto.setAlertName((String) alert.get("alertName"));
            dto.setAlertType((String) alert.get("alertType"));
            dto.setEventType((String) alert.get("eventType"));
        }

        String type = latest.getMessageType().toLowerCase();

        if ("sms".equals(type)) {
            dto.setSmsTemplate(
                    (String) getContent(parseJson(latest.getRawContent()), "sms", "raw")
            );
        } else {
            dto.setEmailTemplate(
                    (String) getContent(parseJson(latest.getRawContent()), "email", "raw")
            );
        }

        return dto;
    }

    public List<FetchAllTemplatesResponseDTO> fetchAllTemplates() {

        List<TemplateMaster> list = templateMasterRepository.findByIsActive("1");

        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }

        //Sort latest first (optional but recommended)
        list.sort(Comparator.comparing(this::getTime).reversed());

        List<FetchAllTemplatesResponseDTO> response = new ArrayList<>();

        for (TemplateMaster entity : list) {
            response.add(mapDirectRecord(entity));
        }

        return response;
    }

    private FetchAllTemplatesResponseDTO mapDirectRecord(TemplateMaster entity) {

        FetchAllTemplatesResponseDTO dto = new FetchAllTemplatesResponseDTO();

        dto.setTemplateName(entity.getTemplateName());
        dto.setMsgType(entity.getMessageType());

        //Alert config
        Map<String, Object> alert = parseJson(entity.getAlertConfig());

        if (alert != null) {
            dto.setDomain((String) alert.get("domain"));
            dto.setAlertName((String) alert.get("alertName"));
            dto.setAlertType((String) alert.get("alertType"));
            dto.setEventType((String) alert.get("eventType"));
        }

        //Type-based mapping
        String type = entity.getMessageType() != null
                ? entity.getMessageType().toLowerCase()
                : "";

        if ("sms".equals(type)) {
            dto.setSmsTemplate(
                    (String) getContent(parseJson(entity.getRawContent()), "sms", "raw")
            );
        }
        else if ("email".equals(type)) {
            dto.setEmailTemplate(
                    (String) getContent(parseJson(entity.getRawContent()), "email", "raw")
            );
        }
        else if ("both".equals(type)) {
            // Handle BOTH row (check actual content)
            Map<String, Object> raw = parseJson(entity.getRawContent());

            if (raw != null) {
                if (raw.containsKey("rawcontent_sms")) {
                    dto.setSmsTemplate((String) raw.get("rawcontent_sms"));
                }
                if (raw.containsKey("rawcontent_email")) {
                    dto.setEmailTemplate((String) raw.get("rawcontent_email"));
                }
            }
        }

        return dto;
    }
}
