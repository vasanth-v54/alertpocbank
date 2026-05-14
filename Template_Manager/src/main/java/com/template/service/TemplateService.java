package com.template.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.template.dto.*;
import com.template.entity.EmailTemplate;
import com.template.entity.SmsTemplate;
import com.template.entity.TemplateMaster;
import com.template.enums.DXP_Status;
import com.template.exception.ResourceNotFoundException;
import com.template.repository.EmailTemplateRepository;
import com.template.repository.SmsTemplateRepository;
import com.template.repository.TemplateMasterRepository;
import com.template.util.JsonUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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


    @Transactional
    public TemplateToggleStatusResponseDTO toggleTemplateStatus(TemplateToggleStatusRequestDTO dto) {
        boolean hasSms = dto.getTemplate_name() != null && dto.getTemplate_name().getSms() != null;
        boolean hasEmail = dto.getTemplate_name() != null && dto.getTemplate_name().getEmail() != null;
        
        Long id = null;

        if (hasSms) {
            id = toggleSingle(dto.getTemplate_name().getSms(), dto, "SMS");
        }
        if (hasEmail) {
            Long emailId = toggleSingle(dto.getTemplate_name().getEmail(), dto, "EMAIL");
            if (id == null) id = emailId;
        }

        if (id == null) {
            throw new ResourceNotFoundException("Active template not found with provided version.");
        }

        return TemplateToggleStatusResponseDTO.builder()
                .success(true)
                .id(id)
                .newStatus("INACTIVE")
                .build();
    }

    private Long toggleSingle(String providedName, TemplateToggleStatusRequestDTO dto, String channelType) {
        String baseName = getBaseName(providedName);
        String fullSearchName = baseName + "_v" + dto.getVersion();

        Optional<TemplateMaster> currentOpt = masterRepo.findByTemplateNameAndVersionAndIsActive(
                fullSearchName, dto.getVersion(), "1");
        if (currentOpt.isEmpty()) {
            currentOpt = masterRepo.findByTemplateNameAndVersionAndIsActive(
                    baseName, dto.getVersion(), "1");
        }

        if (currentOpt.isEmpty()) return null;
        TemplateMaster current = currentOpt.get();

        if (dto.getStatus() != DXP_Status.INACTIVE) {
            throw new RuntimeException("Invalid target status. Only ACTIVE -> INACTIVE transition is allowed.");
        }

        String nextVersion = incrementPatchVersion(current.getVersion());
        String nextFullName = baseName + "_v" + nextVersion;
        String oldFullName = current.getTemplateName();

        current.setIsActive("0");
        current.setVersion(nextVersion);
        current.setTemplateName(nextFullName);
        current.setModifiedBy(dto.getPerformedBy());
        current.setModifiedDate(LocalDateTime.now());
        masterRepo.save(current);

        updateChannelTemplate(oldFullName, nextFullName, false, channelType);
        return current.getId();
    }

    @Transactional
    public TemplateUpdateResponseDTO updateTemplate(TemplateUpdateRequestDTO dto) {
        String msgType = dto.getMessageType() != null ? dto.getMessageType().toUpperCase() : "SMS";
        String nextVersion = incrementMinorVersion(dto.getVersion() != null ? dto.getVersion() : "1.0.0");
        String targetStatus = (dto.getStatus() != null) ?
                ("INACTIVE".equalsIgnoreCase(dto.getStatus()) ? "0" : "1") : "1";

        Long savedId = null;
        String savedTemplateName = null;

        if ("SMS".equals(msgType) || "BOTH".equals(msgType)) {
            String smsName = dto.getTemplate_name() != null ? dto.getTemplate_name().getSms() : null;
            if (smsName != null) {
                TemplateMaster savedSms = updateSingle(dto, smsName, "SMS", nextVersion, targetStatus);
                savedId = savedSms.getId();
                savedTemplateName = savedSms.getTemplateName();
            }
        }

        if ("EMAIL".equals(msgType) || "BOTH".equals(msgType)) {
            String emailName = dto.getTemplate_name() != null ? dto.getTemplate_name().getEmail() : null;
            if (emailName != null) {
                TemplateMaster savedEmail = updateSingle(dto, emailName, "EMAIL", nextVersion, targetStatus);
                if (savedId == null) {
                    savedId = savedEmail.getId();
                    savedTemplateName = savedEmail.getTemplateName();
                }
            }
        }

        return TemplateUpdateResponseDTO.builder()
                .success(true)
                .id(savedId)
                .templateId(savedTemplateName)
                .version(nextVersion)
                .status("1".equals(targetStatus) ? "ACTIVE" : "INACTIVE")
                .message("Template version " + nextVersion + " created successfully")
                .build();
    }

    private TemplateMaster updateSingle(TemplateUpdateRequestDTO dto, String providedName, String channelType, String nextVersion, String targetStatus) {
        String baseName = getBaseName(providedName);
        String searchVersion = dto.getVersion();
        String searchFullName = baseName + "_v" + searchVersion;

        Optional<TemplateMaster> previousOpt = masterRepo.findByTemplateNameAndVersion(searchFullName, searchVersion);
        if (previousOpt.isEmpty()) {
            previousOpt = masterRepo.findByTemplateNameAndVersion(baseName, searchVersion);
        }
        if (previousOpt.isEmpty()) {
            previousOpt = masterRepo.findByTemplateNameStartingWithAndMessageType(baseName, "BOTH".equals(dto.getMessageType()) ? "BOTH" : channelType)
                    .stream().max((t1, t2) -> compareVersions(t1.getVersion(), t2.getVersion()));
        }

        TemplateMaster previous = previousOpt.orElse(null);

        if (previous != null && "1".equals(previous.getIsActive())) {
            String retiredVersion = incrementPatchVersion(previous.getVersion());
            String retiredFullName = baseName + "_v" + retiredVersion;
            String oldFullName = previous.getTemplateName();

            previous.setIsActive("0");
            previous.setVersion(retiredVersion);
            previous.setTemplateName(retiredFullName);
            previous.setModifiedBy("SYSTEM");
            previous.setModifiedDate(LocalDateTime.now());
            masterRepo.save(previous);

            updateChannelTemplate(oldFullName, retiredFullName, false, channelType);
        }

        String nextFullName = baseName + "_v" + nextVersion;
        TemplateMaster nextVersionTemplate = new TemplateMaster();
        nextVersionTemplate.setTemplateName(nextFullName);
        nextVersionTemplate.setMessageType("BOTH".equals(dto.getMessageType()) ? "BOTH" : dto.getMessageType());
        nextVersionTemplate.setVersion(nextVersion);
        nextVersionTemplate.setIsActive(targetStatus);

        Map<String, Object> hMap = null, rMap = null, iMap = null, pMap = null;
        try {
            hMap = dto.getHeaders() instanceof Map ? (Map<String, Object>) dto.getHeaders() : parseJson(toJson(dto.getHeaders()));
            rMap = dto.getRaw_content() instanceof Map ? (Map<String, Object>) dto.getRaw_content() : parseJson(toJson(dto.getRaw_content()));
            iMap = dto.getIndexed_content() instanceof Map ? (Map<String, Object>) dto.getIndexed_content() : parseJson(toJson(dto.getIndexed_content()));
            pMap = dto.getParam_mapping() instanceof Map ? (Map<String, Object>) dto.getParam_mapping() : parseJson(toJson(dto.getParam_mapping()));
        } catch (Exception e) {}

        Map<String, Object> filteredHeaders = extractChannelData(hMap, channelType);
        Map<String, Object> filteredRaw = extractChannelData(rMap, channelType);
        Map<String, Object> filteredIdx = extractChannelData(iMap, channelType);
        Map<String, Object> filteredParam = extractChannelData(pMap, channelType);

        nextVersionTemplate.setAlertConfig(dto.getAlert_config() != null ? toJson(dto.getAlert_config()) : (previous != null ? previous.getAlertConfig() : null));
        nextVersionTemplate.setHeaders(filteredHeaders != null && !filteredHeaders.isEmpty() ? toJson(filteredHeaders) : (previous != null ? previous.getHeaders() : null));
        nextVersionTemplate.setRawContent(filteredRaw != null && !filteredRaw.isEmpty() ? toJson(filteredRaw) : (previous != null ? previous.getRawContent() : null));
        nextVersionTemplate.setIndexedContent(filteredIdx != null && !filteredIdx.isEmpty() ? toJson(filteredIdx) : (previous != null ? previous.getIndexedContent() : null));
        nextVersionTemplate.setParamMapping(filteredParam != null && !filteredParam.isEmpty() ? toJson(filteredParam) : (previous != null ? previous.getParamMapping() : null));
        nextVersionTemplate.setContentHash(dto.getContentHash());
        nextVersionTemplate.setExceptionReason(dto.getExceptionReason());
        nextVersionTemplate.setDuplicateAllowed(dto.getIsDuplicateallowed() != null ? dto.getIsDuplicateallowed() : (previous != null ? previous.getDuplicateAllowed() : false));

        nextVersionTemplate.setCreatedBy(dto.getCreatedBy() != null ? dto.getCreatedBy() : "SYSTEM");
        nextVersionTemplate.setCreatedDate(LocalDateTime.now());
        nextVersionTemplate.setModifiedBy(null);
        nextVersionTemplate.setModifiedDate(null);

        TemplateMaster saved = masterRepo.save(nextVersionTemplate);

        boolean isChannelActive = "1".equals(targetStatus);
        saveChannelTemplate(nextFullName, extractSpecificKey(dto.getIndexed_content(), "indexed_content_" + channelType.toLowerCase()), channelType, isChannelActive);

        return saved;
    }

    private void updateChannelTemplate(String oldName, String newName, boolean active, String type) {
        if ("SMS".equalsIgnoreCase(type) || "BOTH".equalsIgnoreCase(type)) {
            smsRepo.findByTemplateName(oldName).ifPresent(sms -> {
                sms.setTemplateName(newName);
                sms.setActive(active);
                sms.setDateUpdated(LocalDateTime.now());
                smsRepo.save(sms);
            });
        }
        if ("EMAIL".equalsIgnoreCase(type) || "BOTH".equalsIgnoreCase(type)) {
            emailRepo.findByTemplateName(oldName).ifPresent(email -> {
                email.setTemplateName(newName);
                email.setActive(active);
                email.setDateUpdated(LocalDateTime.now());
                emailRepo.save(email);
            });
        }
    }

    private void saveChannelTemplate(String name, String body, String type, boolean active) {
        if ("SMS".equalsIgnoreCase(type)) {
            SmsTemplate sms = new SmsTemplate();
            sms.setTemplateName(name);
            sms.setTemplateBody(body);
            sms.setActive(active);
            sms.setDateCreated(LocalDateTime.now());
            sms.setDateUpdated(LocalDateTime.now());
            smsRepo.save(sms);
        } else {
            EmailTemplate email = new EmailTemplate();
            email.setTemplateName(name);
            email.setTemplateBody(body);
            email.setActive(active);
            email.setDateCreated(LocalDateTime.now());
            email.setDateUpdated(LocalDateTime.now());
            emailRepo.save(email);
        }
    }

    private String incrementPatchVersion(String v) {
        if (v == null || v.isEmpty()) return "1.0.1";
        String clean = v.replace("v", "");
        String[] p = clean.split("\\.");
        if (p.length >= 3) return p[0] + "." + p[1] + "." + (Integer.parseInt(p[2]) + 1);
        if (p.length == 2) return p[0] + "." + p[1] + ".1";
        return p[0] + ".0.1";
    }

    private String incrementMinorVersion(String v) {
        if (v == null || v.isEmpty()) return "1.1.0";
        String clean = v.replace("v", "");
        String[] p = clean.split("\\.");
        if (p.length >= 2) return p[0] + "." + (Integer.parseInt(p[1]) + 1) + ".0";
        return p[0] + ".1.0";
    }

    private String getBaseName(String name) {
        if (name == null) return "";
        return name.contains("_v") ? name.split("_v")[0] : name;
    }

    private String extractSpecificKey(Object obj, String key) {
        try {
            if (obj == null) return null;
            JsonNode node = objectMapper.valueToTree(obj);
            if (node.has(key)) {
                ObjectNode result = objectMapper.createObjectNode();
                result.set(key, node.get(key));
                return objectMapper.writeValueAsString(result);
            }
        } catch (Exception e) {}
        return toJson(obj);
    }

    private String toJson(Object obj) {
        try {
            if (obj instanceof String) return (String) obj;
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return null;
        }
    }

    private int compareVersions(String v1, String v2){
        try {
            String[] p1 = v1.replace("v", "").split("\\.");
            String[] p2 = v2.replace("v", "").split("\\.");
            for (int i = 0; i < Math.max(p1.length, p2.length); i++) {
                int n1 = i < p1.length ? Integer.parseInt(p1[i]) : 0;
                int n2 = i < p2.length ? Integer.parseInt(p2[i]) : 0;
                if (n1 != n2) return Integer.compare(n1, n2);
            }
            return 0;
        } catch (Exception e) {
            return 0;
        }
    }
}
