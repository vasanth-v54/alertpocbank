package com.template.service;

import com.template.dto.*;
import com.template.entity.EmailTemplate;
import com.template.entity.SmsTemplate;
import com.template.entity.TemplateMaster;
import com.template.enums.DXP_Status;
import com.template.repository.EmailTemplateRepository;
import com.template.repository.SmsTemplateRepository;
import com.template.repository.TemplateMasterRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TemplateService {

    @Autowired
    private SmsTemplateRepository smsTemplateRepository;

    @Autowired
    private EmailTemplateRepository emailTemplateRepository;

    @Autowired
    private TemplateMasterRepository templateMasterRepository;

    @Transactional
    public TemplateToggleStatusResponseDTO toggleTemplateStatus(Long id,
            TemplateToggleStatusRequestDTO templateToggleStatusRequestDTO) {
        TemplateMaster template = templateMasterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Template not found with ID: " + id));

        DXP_Status requestedStatus = templateToggleStatusRequestDTO.getStatus();

        if ("SMS".equalsIgnoreCase(template.getMessageType())) {
            if (requestedStatus == DXP_Status.ACTIVE) {
                activateTemplateIfMessageTypeIsSMS(template, templateToggleStatusRequestDTO);
            } else {
                deactivateTempalateIfMessageTypeIsSMS(id, template.getTemplateName(),
                        templateToggleStatusRequestDTO);
            }

        } else if ("EMAIL".equalsIgnoreCase(template.getMessageType())) {

            if (requestedStatus == DXP_Status.ACTIVE) {
                activateTemplateIfMessageTypeIsEMAIL(template, templateToggleStatusRequestDTO);
            } else {
                deactivateTemplateIfMessageTypeIsEMAIL(id, template.getTemplateName(),
                        templateToggleStatusRequestDTO);
            }

        } else if ("BOTH".equalsIgnoreCase(template.getMessageType())) {
            if (requestedStatus == DXP_Status.ACTIVE){
                activateTemplateIfMessageTypeIsBOTH(id, template, templateToggleStatusRequestDTO);
            } else {
                deactivateTemplateIfMessageTypeIsBOTH(id, template.getTemplateName(),
                        templateToggleStatusRequestDTO);
            }

        }else {
            throw new RuntimeException("Unsupported message type: " + template.getMessageType());
        }

        return TemplateToggleStatusResponseDTO.builder()
                .success(true)
                .id(id)
                .newStatus(requestedStatus)
                .build();
    }

    public String convertLongToString(long value) {
        return String.valueOf(value);
    }

    @Transactional
    public int deactivateTempalateIfMessageTypeIsSMS(Long id, String templateName,
            TemplateToggleStatusRequestDTO templateToggleStatusRequestDTO) {
        int updatedRawsInSMS = smsTemplateRepository.deactivateTemplate(templateName);
        int updatedRawsInTemplateMaster = templateMasterRepository.deactivateById(id,
                templateToggleStatusRequestDTO.getPerformedBy());

        // Smart Sync: Only error if NOTHING was changed
        if (updatedRawsInSMS == 0 && updatedRawsInTemplateMaster == 0) {
            throw new RuntimeException("Both SMS and Master templates were already INACTIVE with id: " + id);
        }

        return (updatedRawsInTemplateMaster == 1 || updatedRawsInSMS == 1) ? 1 : 0;
    }

    @Transactional
    public int deactivateTemplateIfMessageTypeIsEMAIL(Long id, String templateName, TemplateToggleStatusRequestDTO dto) {
        int updatedRowsInEmail = emailTemplateRepository.deactivateTemplate(templateName);
        int updatedRowsInTemplateMaster = templateMasterRepository.deactivateById(id, dto.getPerformedBy());

        if (updatedRowsInEmail == 0 && updatedRowsInTemplateMaster == 0) {
            throw new RuntimeException("Both Email and Master templates were already INACTIVE with id: " + id);
        }

        return (updatedRowsInEmail == 1 || updatedRowsInTemplateMaster == 1) ? 1 : 0;
    }
    @Transactional
    public int deactivateTemplateIfMessageTypeIsBOTH(Long id, String templateName, TemplateToggleStatusRequestDTO dto) {
        int updatedRawsInSMS = smsTemplateRepository.deactivateTemplate(templateName);
        int updatedRowsInEmail = emailTemplateRepository.deactivateTemplate(templateName);
        int updatedRowsInTemplateMaster = templateMasterRepository.deactivateById(id, dto.getPerformedBy());

        // Nothing Updated
        if (updatedRawsInSMS == 0 && updatedRowsInEmail == 0 && updatedRowsInTemplateMaster == 0) {
            throw new RuntimeException("SMS, Email and Master templates were already INACTIVE with id: " + id);
        }

        // Case 2: All updated
        if (updatedRawsInSMS == 1 && updatedRowsInEmail == 1 && updatedRowsInTemplateMaster == 1) {
            return 1;
        }

        // Case 3: Partial update (INCONSISTENT STATE)
        throw new RuntimeException(
                "Partial update detected! SMS: " + updatedRawsInSMS +
                        ", EMAIL: " + updatedRowsInEmail +
                        ", MASTER: " + updatedRowsInTemplateMaster +
                        " for id: " + id
        );
    }
    @Transactional
    public int activateTemplateIfMessageTypeIsSMS(TemplateMaster templateToActivate, TemplateToggleStatusRequestDTO dto) {
        // Sweep: Deactivate any other active versions
        deactivateOtherActiveVersions(templateToActivate.getTemplateName(), "SMS", dto.getPerformedBy());

        int updatedRowsInSMS = smsTemplateRepository.activateTemplate(templateToActivate.getTemplateName());
        int updatedRowsInTemplateMaster = templateMasterRepository.activateById(templateToActivate.getId(), dto.getPerformedBy());

        return (updatedRowsInTemplateMaster == 1 || updatedRowsInSMS == 1) ? 1 : 0;
    }

    @Transactional
    public int activateTemplateIfMessageTypeIsEMAIL(TemplateMaster templateToActivate, TemplateToggleStatusRequestDTO dto) {
        // Sweep: Deactivate any other active versions
        deactivateOtherActiveVersions(templateToActivate.getTemplateName(), "EMAIL", dto.getPerformedBy());

        int updatedRowsInEmail = emailTemplateRepository.activateTemplate(templateToActivate.getTemplateName());
        int updatedRowsInTemplateMaster = templateMasterRepository.activateById(templateToActivate.getId(), dto.getPerformedBy());

        return (updatedRowsInTemplateMaster == 1 || updatedRowsInEmail == 1) ? 1 : 0;
    }
    @Transactional
    public int activateTemplateIfMessageTypeIsBOTH(Long id, TemplateMaster templateToActivate, TemplateToggleStatusRequestDTO dto) {
        // Sweep: Deactivate any other active versions
        deactivateOtherActiveVersions(templateToActivate.getTemplateName(), "BOTH", dto.getPerformedBy());


        int updatedRawsInSMS = smsTemplateRepository.activateTemplate(templateToActivate.getTemplateName());
        int updatedRowsInEmail = emailTemplateRepository.activateTemplate(templateToActivate.getTemplateName());
        int updatedRowsInTemplateMaster = templateMasterRepository.activateById(templateToActivate.getId(), dto.getPerformedBy());

        // Nothing Updated
        if (updatedRawsInSMS == 0 && updatedRowsInEmail == 0 && updatedRowsInTemplateMaster == 0) {
            throw new RuntimeException("SMS, Email and Master templates were already INACTIVE with id: " + id);
        }

        // Case 2: All updated
        if (updatedRawsInSMS == 1 && updatedRowsInEmail == 1 && updatedRowsInTemplateMaster == 1) {
            return 1;
        }

        // Case 3: Partial update (INCONSISTENT STATE)
        throw new RuntimeException(
                "Partial update detected! SMS: " + updatedRawsInSMS +
                        ", EMAIL: " + updatedRowsInEmail +
                        ", MASTER: " + updatedRowsInTemplateMaster +
                        " for id: " + id
        );
    }


    private void deactivateOtherActiveVersions(String templateName, String messageType, String performedBy) {
        List<TemplateMaster> actives = templateMasterRepository.findAllByTemplateNameAndMessageTypeAndIsActive(
                templateName, messageType, true);
        for (TemplateMaster active : actives) {
            active.setIsActive(false);
            active.setModifiedBy(performedBy);
            active.setModifiedDate(LocalDateTime.now());
            templateMasterRepository.save(active);

            if ("SMS".equalsIgnoreCase(messageType)) {
                smsTemplateRepository.deactivateTemplate(active.getTemplateName());
            } else if ("EMAIL".equalsIgnoreCase(messageType)) {
                emailTemplateRepository.deactivateTemplate(active.getTemplateName());
            } else if ("BOTH".equalsIgnoreCase(messageType)){
                smsTemplateRepository.deactivateTemplate(active.getTemplateName());
                emailTemplateRepository.deactivateTemplate(active.getTemplateName());
            }
        }
    }

    @Transactional
    public TemplateUpdateResponseDTO updateTemplate(Long id, TemplateUpdateRequestDTO dto) {
        TemplateMaster current = templateMasterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Template not found with ID: " + id));

        String nextVersion = incrementVersion(current.getVersion());

        if ("SMS".equalsIgnoreCase(current.getMessageType())) {
            SmsTemplate oldSms = smsTemplateRepository.findByTemplateName(current.getTemplateName())
                    .orElseThrow(() -> new RuntimeException("SMS Template not found with name: " + current.getTemplateName()));

            SmsTemplate newSms = new SmsTemplate();
            newSms.setTemplateName(oldSms.getTemplateName().split("_v")[0] + "_" + nextVersion);
            newSms.setTemplateBody(dto.getRawContent() != null ? dto.getRawContent() : oldSms.getTemplateBody());
            String status = dto.getStatus() != null ? dto.getStatus() : "ACTIVE";
            newSms.setIsActive("ACTIVE".equalsIgnoreCase(status));
            newSms.setDateCreated(LocalDateTime.now());
            newSms.setDateUpdated(LocalDateTime.now());
            newSms = smsTemplateRepository.save(newSms);


            smsTemplateRepository.deactivateTemplate(oldSms.getTemplateName());

        } else if ("EMAIL".equalsIgnoreCase(current.getMessageType())) {
            EmailTemplate oldEmail = emailTemplateRepository.findByTemplateName(current.getTemplateName())
                    .orElseThrow(() -> new RuntimeException("Email Template not found with name: " + current.getTemplateName()));

            EmailTemplate newEmail = new EmailTemplate();
            newEmail.setTemplateName(oldEmail.getTemplateName().split("_v")[0] + "_" + nextVersion);
            newEmail.setTemplateBody(dto.getRawContent() != null ? dto.getRawContent() : oldEmail.getTemplateBody());
            String status = dto.getStatus() != null ? dto.getStatus() : "ACTIVE";
            newEmail.setIsActive("ACTIVE".equalsIgnoreCase(status));
            newEmail.setDateCreated(LocalDateTime.now());
            newEmail.setDateUpdated(LocalDateTime.now());
            newEmail = emailTemplateRepository.save(newEmail);

            emailTemplateRepository.deactivateTemplate(oldEmail.getTemplateName());

        } else if ("BOTH".equalsIgnoreCase(current.getMessageType())) {

            SmsTemplate oldSms = smsTemplateRepository.findByTemplateName(current.getTemplateName())
                    .orElseThrow(() -> new RuntimeException("SMS Template not found with name: " + current.getTemplateName()));

            EmailTemplate oldEmail = emailTemplateRepository.findByTemplateName(current.getTemplateName())
                    .orElseThrow(() -> new RuntimeException("Email Template not found with name: " + current.getTemplateName()));

            SmsTemplate newSms = new SmsTemplate();
            newSms.setTemplateName(oldSms.getTemplateName().split("_v")[0] + "_" + nextVersion);
            newSms.setTemplateBody(dto.getRawContent() != null ? dto.getRawContent() : oldSms.getTemplateBody());
            String smsStatus = dto.getStatus() != null ? dto.getStatus() : "ACTIVE";
            newSms.setIsActive("ACTIVE".equalsIgnoreCase(smsStatus));
            newSms.setDateCreated(LocalDateTime.now());
            newSms.setDateUpdated(LocalDateTime.now());
            newSms = smsTemplateRepository.save(newSms);

            EmailTemplate newEmail = new EmailTemplate();
            newEmail.setTemplateName(oldEmail.getTemplateName().split("_v")[0] + "_" + nextVersion);
            newEmail.setTemplateBody(dto.getRawContent() != null ? dto.getRawContent() : oldEmail.getTemplateBody());
            String emailStatus = dto.getStatus() != null ? dto.getStatus() : "ACTIVE";
            newEmail.setIsActive("ACTIVE".equalsIgnoreCase(emailStatus));
            newEmail.setDateCreated(LocalDateTime.now());
            newEmail.setDateUpdated(LocalDateTime.now());
            newEmail = emailTemplateRepository.save(newEmail);

            smsTemplateRepository.deactivateTemplate(oldSms.getTemplateName());
            emailTemplateRepository.deactivateTemplate(oldEmail.getTemplateName());
        }

        TemplateMaster nextVersionTemplate = TemplateMaster.builder()
                .templateName(current.getTemplateName())
                .messageType(current.getMessageType())
                .version(nextVersion)
                .alertConfig(dto.getAlertConfig() != null ? dto.getAlertConfig() : current.getAlertConfig())
                .headers(dto.getHeaders() != null ? dto.getHeaders() : current.getHeaders())
//                .rawContent(dto.getRawContent() != null ? dto.getRawContent() : current.getRawContent())
//                .indexedContent(current.getIndexedContent())
                .rawContent(dto.getRawContent() != null ? toJson(dto.getRawContent()) : current.getRawContent())
                .indexedContent(current.getIndexedContent())
                .paramMapping(dto.getParamMapping() != null ? dto.getParamMapping() : current.getParamMapping())
                .isDuplicateAllowed(dto.getIsDuplicateAllowed() != null ? dto.getIsDuplicateAllowed() : current.getIsDuplicateAllowed())
                .isActive("ACTIVE".equalsIgnoreCase(dto.getStatus() != null ? dto.getStatus() : "ACTIVE"))
                .createdBy(current.getCreatedBy())
                .modifiedBy(dto.getPerformedBy())
                .createdDate(current.getCreatedDate())
                .modifiedDate(LocalDateTime.now())
                .build();

        if (nextVersionTemplate.getIsActive()) {
            deactivateOtherActiveVersions(current.getTemplateName(), current.getMessageType(), dto.getPerformedBy());
        }

        TemplateMaster saved = templateMasterRepository.save(nextVersionTemplate);

        return TemplateUpdateResponseDTO.builder()
                .success(true)
                .id(saved.getId())
                .version(saved.getVersion())
                .status(saved.getIsActive() ? "ACTIVE" : "INACTIVE")
                .message("Template version " + nextVersion + " created successfully")
                .build();
    }

    private String incrementVersion(String currentVersion) {
        if (currentVersion == null || !currentVersion.contains("v")) {
            return "v1.1";
        }
        try {
            String v = currentVersion.replace("v", "");
            double versionNum = Double.parseDouble(v);
            return "v" + String.format("%.1f", versionNum + 0.1);
        } catch (Exception e) {
            return currentVersion + ".1";
        }
    }

    private String toJson(String value) {
        if (value == null) return null;
        // Already valid JSON (starts with { or [ or is quoted)
        String trimmed = value.trim();
        if (trimmed.startsWith("{") || trimmed.startsWith("[") || trimmed.startsWith("\"")) {
            return value;
        }
        // Plain string — wrap it
        return "{\"body\": " + "\"" + value.replace("\"", "\\\"") + "\"}";
    }
}