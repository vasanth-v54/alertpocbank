package com.template.service;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Service
public class TemplateService1 {

    @Autowired
    private ObjectMapper objectMapper;

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


    @Transactional
    public TemplateToggleStatusResponseDTO toggleTemplateStatus(TemplateToggleStatusRequestDTO dto) {
        // Business logic: Find by template_name, version and isActive instead of ID
        String baseName = dto.getTemplate_name().getSms() != null ?
                dto.getTemplate_name().getSms() : dto.getTemplate_name().getEmail();
        String fullSearchName = baseName + "_v" + dto.getVersion();

        // Try versioned name first (e.g. mytemplate_v1.0.0), then fall back to plain name (e.g. mytemplate)
        Optional<TemplateMaster> currentOpt = masterRepo.findByTemplateNameAndVersionAndIsActive(
                fullSearchName, dto.getVersion(), "1");
        if (currentOpt.isEmpty()) {
            currentOpt = masterRepo.findByTemplateNameAndVersionAndIsActive(
                    baseName, dto.getVersion(), "1");
        }

        TemplateMaster current = currentOpt
                .orElseThrow(() -> new ResourceNotFoundException("Active template not found with name: " +
                        baseName + " (or " + fullSearchName + ") and version: " + dto.getVersion()));

        // Transition: ACTIVE -> INACTIVE only
        if (dto.getStatus() != DXP_Status.INACTIVE) {
            throw new RuntimeException("Invalid target status. Only ACTIVE -> INACTIVE transition is allowed.");
        }

        // Versioning: increment last digit for status toggle
        String nextVersion = incrementPatchVersion(current.getVersion());
        String nextFullName = baseName + "_v" + nextVersion;
        String oldFullName = current.getTemplateName();

        // Update TemplateMaster (Natural key identity change)
        current.setIsActive("0");
        current.setVersion(nextVersion);
        current.setTemplateName(nextFullName);
        current.setModifiedBy(dto.getPerformedBy());
        current.setModifiedDate(LocalDateTime.now());
        masterRepo.save(current);

        // Update Channel Tables
        if ("SMS".equalsIgnoreCase(current.getMessageType()) || "BOTH".equalsIgnoreCase(current.getMessageType())) {
            updateChannelTemplate(oldFullName, nextFullName, false, "SMS");
        }
        if ("EMAIL".equalsIgnoreCase(current.getMessageType()) || "BOTH".equalsIgnoreCase(current.getMessageType())) {
            updateChannelTemplate(oldFullName, nextFullName, false, "EMAIL");
        }

        return TemplateToggleStatusResponseDTO.builder()
                .success(true)
                .id(current.getId())
                .newStatus("INACTIVE")
                .build();
    }

    @Transactional
    public TemplateUpdateResponseDTO updateTemplate(TemplateUpdateRequestDTO dto) {
        // Find previous version to get missing data
        String smsName = dto.getTemplate_name() != null ? dto.getTemplate_name().getSms() : null;
        String emailName = dto.getTemplate_name() != null ? dto.getTemplate_name().getEmail() : null;
        String baseName = getBaseName(smsName != null ? smsName : emailName);

        // Use business keys to find the version we are updating from
        String searchVersion = dto.getVersion();
        String searchFullName = baseName + "_v" + searchVersion;

        // Try versioned name first (e.g. mytemplate_v1.0.0), then plain name (e.g. mytemplate)
        Optional<TemplateMaster> previousOpt = masterRepo.findByTemplateNameAndVersion(searchFullName, searchVersion);
        if (previousOpt.isEmpty()) {
            previousOpt = masterRepo.findByTemplateNameAndVersion(baseName, searchVersion);
        }
        if (previousOpt.isEmpty()) {
            // Fallback: Find latest version of this template family
            previousOpt = masterRepo.findByTemplateNameStartingWithAndMessageType(baseName, dto.getMessageType())
                    .stream().max((t1, t2) -> compareVersions(t1.getVersion(), t2.getVersion()));
        }

        TemplateMaster previous = previousOpt.orElse(null);

        // Logic 1: If we are changing status from active to inactive, update version
        // Logic 2: Edits always increment middle value (1.0.0 -> 1.1.0)
        String currentVersion = (previous != null) ? previous.getVersion() : (dto.getVersion() != null ? dto.getVersion() : "1.0.0");
        String nextVersion = incrementMinorVersion(currentVersion);
        String nextFullName = baseName + "_v" + nextVersion;

        // Inactivate previous version if it was active
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

            // Update channel tables for the retired record
            updateChannelTemplate(oldFullName, retiredFullName, false, previous.getMessageType());
        }

        // Create New TemplateMaster Record (Targeting new version)
        TemplateMaster nextVersionTemplate = new TemplateMaster();
        nextVersionTemplate.setTemplateName(nextFullName);
        nextVersionTemplate.setMessageType(dto.getMessageType());
        nextVersionTemplate.setVersion(nextVersion);

        // Take status from DTO if provided, otherwise default to ACTIVE (1)
        String targetStatus = (dto.getStatus() != null) ?
                ("INACTIVE".equalsIgnoreCase(dto.getStatus()) ? "0" : "1") : "1";
        nextVersionTemplate.setIsActive(targetStatus);

        // Populate dynamic fields
        nextVersionTemplate.setAlertConfig(dto.getAlert_config() != null ? toJson(dto.getAlert_config()) : (previous != null ? previous.getAlertConfig() : null));
        nextVersionTemplate.setHeaders(dto.getHeaders() != null ? toJson(dto.getHeaders()) : (previous != null ? previous.getHeaders() : null));
        nextVersionTemplate.setRawContent(dto.getRaw_content() != null ? toJson(dto.getRaw_content()) : (previous != null ? previous.getRawContent() : null));
        nextVersionTemplate.setIndexedContent(dto.getIndexed_content() != null ? toJson(dto.getIndexed_content()) : (previous != null ? previous.getIndexedContent() : null));
        nextVersionTemplate.setParamMapping(dto.getParam_mapping() != null ? toJson(dto.getParam_mapping()) : (previous != null ? previous.getParamMapping() : null));
        nextVersionTemplate.setContentHash(dto.getContentHash());
        nextVersionTemplate.setExceptionReason(dto.getExceptionReason());
        nextVersionTemplate.setDuplicateAllowed(dto.getIsDuplicateallowed() != null ? dto.getIsDuplicateallowed() : (previous != null ? previous.getDuplicateAllowed() : false));

        // Audit Requirement: CreatedBy provided or SYSTEM. CreatedDate NOW. Modified NULL.
        nextVersionTemplate.setCreatedBy(dto.getCreatedBy() != null ? dto.getCreatedBy() : "SYSTEM");
        nextVersionTemplate.setCreatedDate(LocalDateTime.now());
        nextVersionTemplate.setModifiedBy(null);
        nextVersionTemplate.setModifiedDate(null);

        TemplateMaster saved = masterRepo.save(nextVersionTemplate);

        // Create Channel Records
        boolean isChannelActive = "1".equals(targetStatus);
        if ("SMS".equalsIgnoreCase(dto.getMessageType()) || "BOTH".equalsIgnoreCase(dto.getMessageType())) {
            saveChannelTemplate(nextFullName, extractSpecificKey(dto.getIndexed_content(), "indexed_content_sms"), "SMS", isChannelActive);
        }
        if ("EMAIL".equalsIgnoreCase(dto.getMessageType()) || "BOTH".equalsIgnoreCase(dto.getMessageType())) {
            saveChannelTemplate(nextFullName, extractSpecificKey(dto.getIndexed_content(), "indexed_content_email"), "EMAIL", isChannelActive);
        }

        return TemplateUpdateResponseDTO.builder()
                .success(true)
                .id(saved.getId())
                .templateId(saved.getTemplateName())
                .version(saved.getVersion())
                .status("1".equals(saved.getIsActive()) ? "ACTIVE" : "INACTIVE")
                .message("Template version " + nextVersion + " created successfully")
                .build();
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

    private int compareVersions(String v1, String v2) {
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
