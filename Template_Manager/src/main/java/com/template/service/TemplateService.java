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
import com.template.util.JsonUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private TemplateMasterRepository masterRepo;

    @Autowired
    private SmsTemplateRepository smsRepo;

    @Autowired
    private EmailTemplateRepository emailRepo;

    @Transactional
    public String createTemplate(TemplateRequest request) {

        validate(request);

        Map<String, Object> indexed = request.getIndexed_content();

        String smsContent = (String) indexed.get("indexed_content_sms");
        String emailContent = (String) indexed.get("indexed_content_email");

        switch (request.getMessageType().toUpperCase()) {

            case "SMS" -> {
                saveMaster(request, "SMS", smsContent);
                saveSms(request, smsContent);
            }

            case "EMAIL" -> {
                saveMaster(request, "EMAIL", emailContent);
                saveEmail(request, emailContent);
            }

            case "BOTH" -> {
                //2 rows in template_master (both message_type = BOTH)
                saveMaster(request, "BOTH_SMS", smsContent);
                saveMaster(request, "BOTH_EMAIL", emailContent);

                // Channel tables
                saveSms(request, smsContent);
                saveEmail(request, emailContent);
            }

            default -> throw new RuntimeException("Invalid messageType");
        }

        return "Template Created Successfully";
    }

    // ================= MASTER SAVE =================

    private void saveMaster(TemplateRequest request, String type, String content) {

        if (content == null) return;

        TemplateMaster entity = new TemplateMaster();

        entity.setTemplateName(getTemplateName(request, type));

        //IMPORTANT: always BOTH for both rows
        entity.setMessageType(
                request.getMessageType().equalsIgnoreCase("BOTH")
                        ? "BOTH"
                        : request.getMessageType()
        );

        entity.setVersion(request.getVersion());

        entity.setAlertConfig(JsonUtil.toJson(request.getAlert_config()));

        //Split JSON per channel
        entity.setHeaders(JsonUtil.toJson(extractChannelData(request.getHeaders(), type)));
        entity.setRawContent(JsonUtil.toJson(extractChannelData(request.getRaw_content(), type)));
        entity.setIndexedContent(JsonUtil.toJson(extractChannelData(request.getIndexed_content(), type)));
        entity.setParamMapping(JsonUtil.toJson(extractChannelData(request.getParam_mapping(), type)));

        entity.setContentHash(request.getContentHash());
        entity.setExceptionReason(request.getExceptionReason());
        entity.setDuplicateAllowed(request.getIsDuplicateallowed());

        entity.setIsActive("1");
        entity.setCreatedBy("SYSTEM");
        entity.setModifiedBy("SYSTEM");
        entity.setCreatedDate(LocalDateTime.now());
        entity.setModifiedDate(LocalDateTime.now());

        masterRepo.save(entity);
    }

    // ================= CHANNEL SAVE =================

    private void saveSms(TemplateRequest request, String content) {
        if (content == null) return;

        SmsTemplate sms = new SmsTemplate();
        sms.setTemplateName(request.getTemplate_name().get("sms"));
        sms.setTemplateBody(JsonUtil.toJson(
                Map.of("indexed_content_sms", content)
        ));
        sms.setActive(true);
        sms.setDateCreated(LocalDateTime.now());
        sms.setDateUpdated(LocalDateTime.now());

        smsRepo.save(sms);
    }

    private void saveEmail(TemplateRequest request, String content) {
        if (content == null) return;

        EmailTemplate email = new EmailTemplate();
        email.setTemplateName(request.getTemplate_name().get("email"));
        email.setTemplateBody(JsonUtil.toJson(
                Map.of("indexed_content_email", content)
        ));
        email.setActive(true);
        email.setDateCreated(LocalDateTime.now());
        email.setDateUpdated(LocalDateTime.now());

        emailRepo.save(email);
    }

    // ================= HELPERS =================

    private Map<String, Object> extractChannelData(Map<String, Object> source, String type) {

        if (source == null) return null;

        Map<String, Object> result = new HashMap<>();

        if (type.contains("SMS")) {
            source.forEach((k, v) -> {
                if (k.toLowerCase().contains("sms")) {
                    result.put(k, v);
                }
            });
        }

        if (type.contains("EMAIL")) {
            source.forEach((k, v) -> {
                if (k.toLowerCase().contains("email")) {
                    result.put(k, v);
                }
            });
        }

        return result;
    }

    private String getTemplateName(TemplateRequest request, String type) {

        if (type.contains("SMS")) {
            return request.getTemplate_name().get("sms");
        }

        if (type.contains("EMAIL")) {
            return request.getTemplate_name().get("email");
        }

        return request.getTemplate_name().get("sms"); // fallback
    }

    // ================= VALIDATION =================

    private void validate(TemplateRequest request) {

        if (request.getMessageType() == null) {
            throw new RuntimeException("messageType required");
        }

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

        //  Get latest SMS
        TemplateMaster latestSms = list.stream()
                .filter(t -> containsKey(t.getHeaders(), "headers_sms"))
                .max(Comparator.comparing(this::getTime))
                .orElse(null);

        //  Get latest EMAIL
        TemplateMaster latestEmail = list.stream()
                .filter(t -> containsKey(t.getHeaders(), "headers_email"))
                .max(Comparator.comparing(this::getTime))
                .orElse(null);

        res.setTemplate_name(list.get(0).getTemplateName());
        res.setMessageType("BOTH");
        res.setVersion(list.get(0).getVersion());

        // ================= ID =================
        Map<String, Object> idMap = new HashMap<>();
        if (latestSms != null) idMap.put("sms", latestSms.getId());
        if (latestEmail != null) idMap.put("email", latestEmail.getId());
        res.setId(idMap);

        // ================= ALERT CONFIG (SINGLE) =================
        if (latestSms != null) {
            res.setAlert_config(parseJson(latestSms.getAlertConfig()));
        } else if (latestEmail != null) {
            res.setAlert_config(parseJson(latestEmail.getAlertConfig()));
        }

        // ================= HEADERS =================
        Map<String, Object> headerMap = new HashMap<>();
        if (latestSms != null) {
            headerMap.put("headers_sms",
                    getSafe(parseJson(latestSms.getHeaders()), "sms", "headers_sms"));
        }
        if (latestEmail != null) {
            headerMap.put("headers_email",
                    getSafe(parseJson(latestEmail.getHeaders()), "email", "headers_email"));
        }
        res.setHeaders(headerMap);

        // ================= RAW CONTENT =================
        Map<String, Object> rawMap = new HashMap<>();
        if (latestSms != null) {
            rawMap.put("rawcontent_sms",
                    getContent(parseJson(latestSms.getRawContent()), "sms", "raw"));
        }
        if (latestEmail != null) {
            rawMap.put("rawcontent_email",
                    getContent(parseJson(latestEmail.getRawContent()), "email", "raw"));
        }
        res.setRaw_content(rawMap);

        // ================= INDEXED CONTENT =================
        Map<String, Object> indexMap = new HashMap<>();
        if (latestSms != null) {
            indexMap.put("indexed_content_sms",
                    getContent(parseJson(latestSms.getIndexedContent()), "sms", "indexed"));
        }
        if (latestEmail != null) {
            indexMap.put("indexed_content_email",
                    getContent(parseJson(latestEmail.getIndexedContent()), "email", "indexed"));
        }
        res.setIndexed_content(indexMap);

        // ================= PARAM MAPPING =================
        Map<String, Object> paramMap = new HashMap<>();
        if (latestSms != null) {
            paramMap.put("param_mapping_sms",
                    mapParams(parseJson(latestSms.getParamMapping())));
        }
        if (latestEmail != null) {
            paramMap.put("param_mapping_email",
                    mapParams(parseJson(latestEmail.getParamMapping())));
        }
        res.setParam_mapping(paramMap);

        // ================= OPTIONAL FIELDS =================
        res.setContentHash(null);
        res.setExceptionReason(null);
        res.setIsDuplicateallowed(null);

        return res;
    }


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

        Map<String, List<TemplateMaster>> grouped =
                list.stream().collect(Collectors.groupingBy(TemplateMaster::getTemplateName));

        List<FetchAllTemplatesResponseDTO> response = new ArrayList<>();

        for (Map.Entry<String, List<TemplateMaster>> entry : grouped.entrySet()) {

            List<TemplateMaster> records = entry.getValue();

            boolean hasBoth = records.stream()
                    .anyMatch(t -> "BOTH".equalsIgnoreCase(t.getMessageType()));

            boolean hasSms = records.stream()
                    .anyMatch(t -> containsKey(t.getHeaders(), "headers_sms"));

            boolean hasEmail = records.stream()
                    .anyMatch(t -> containsKey(t.getHeaders(), "headers_email"));

            if (hasBoth && hasSms && hasEmail) {

                response.add(mapBothForFetchAll(records));

            } else {

                for (TemplateMaster entity : records) {
                    response.add(mapDirectRecord(entity));
                }
            }
        }

        return response;
    }

    private FetchAllTemplatesResponseDTO mapBothForFetchAll(List<TemplateMaster> list) {

        TemplateMaster latestSms = list.stream()
                .filter(t -> containsKey(t.getHeaders(), "headers_sms"))
                .max(Comparator.comparing(this::getTime))
                .orElse(null);

        TemplateMaster latestEmail = list.stream()
                .filter(t -> containsKey(t.getHeaders(), "headers_email"))
                .max(Comparator.comparing(this::getTime))
                .orElse(null);

        FetchAllTemplatesResponseDTO dto = new FetchAllTemplatesResponseDTO();

        // ALWAYS JSON (WITH NULL SAFETY)
        Map<String, String> nameMap = new HashMap<>();
        nameMap.put("sms", latestSms != null ? latestSms.getTemplateName() : null);
        nameMap.put("email", latestEmail != null ? latestEmail.getTemplateName() : null);

        dto.setTemplateName(nameMap);
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

    private FetchAllTemplatesResponseDTO mapDirectRecord(TemplateMaster entity) {

        FetchAllTemplatesResponseDTO dto = new FetchAllTemplatesResponseDTO();

        // ALWAYS JSON
        Map<String, String> nameMap = new HashMap<>();
        nameMap.put("sms", null);
        nameMap.put("email", null);

        String type = entity.getMessageType() != null
                ? entity.getMessageType().toLowerCase()
                : "";

        if ("sms".equals(type)) {
            nameMap.put("sms", entity.getTemplateName());
        } else if ("email".equals(type)) {
            nameMap.put("email", entity.getTemplateName());
        }

        dto.setTemplateName(nameMap);
        dto.setMsgType(entity.getMessageType());

        Map<String, Object> alert = parseJson(entity.getAlertConfig());

        if (alert != null) {
            dto.setDomain((String) alert.get("domain"));
            dto.setAlertName((String) alert.get("alertName"));
            dto.setAlertType((String) alert.get("alertType"));
            dto.setEventType((String) alert.get("eventType"));
        }

        if ("sms".equals(type)) {
            dto.setSmsTemplate(
                    (String) getContent(parseJson(entity.getRawContent()), "sms", "raw")
            );
        } else if ("email".equals(type)) {
            dto.setEmailTemplate(
                    (String) getContent(parseJson(entity.getRawContent()), "email", "raw")
            );
        }

        return dto;
    }
}
