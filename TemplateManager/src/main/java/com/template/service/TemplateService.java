package com.template.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.template.dto.*;
import com.template.entity.TemplateMaster;
import com.template.repository.TemplateRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class TemplateService {

    private final TemplateRepository repository;
    private final ObjectMapper objectMapper;

    public TemplateResponseDTO getTemplate(String templateName) {

        log.info("Fetching template for templateName: {}", templateName);

        List<TemplateMaster> templates = repository
                .findByTemplateNameAndIsActive(templateName, "1");

        if (templates == null || templates.isEmpty()) {
            log.error("Template not found: {}", templateName);
            throw new RuntimeException("Template not found");
        }
        TemplateMaster entity = templates.get(0);

        try {

            DataDTO data = buildDataDTO(entity);

            TemplateResponseDTO response = new TemplateResponseDTO();
            response.setSuccess(true);
            response.setMessage("Success");
            response.setData(data);

            log.info("Template fetched successfully: {}", templateName);

            return response;

        } catch (Exception e) {
            log.error("Error while processing template: {}", templateName, e);
            throw new RuntimeException("Error processing template", e);
        }
    }

    private DataDTO buildDataDTO(TemplateMaster entity) throws Exception {

        DataDTO data = new DataDTO();

        data.setTemplate_name(entity.getTemplateName());
        data.setMessageType(entity.getMessageType());
        data.setVersion(entity.getVersion());
        data.setContentHash(entity.getContentHash());
        data.setExceptionReason(entity.getExceptionReason());
        data.setIsDuplicateallowed(entity.isDuplicateallowed());

        data.setAlert_config(parseJson(entity.getAlertConfig(), AlertConfigDTO.class));
        data.setHeaders(parseJson(entity.getHeaders(), HeadersDTO.class));
        data.setRaw_content(parseJson(entity.getRawContent(), RawContentDTO.class));
        data.setIndexed_content(parseJson(entity.getIndexedContent(), IndexedContentDTO.class));
        data.setParam_mapping(parseJson(entity.getParamMapping(), ParamMappingDTO.class));

        // VALIDATIONS
        validateData(data, entity.getMessageType());

        return data;
    }


    private <T> T parseJson(String json, Class<T> clazz) throws Exception {

        if (json == null || json.isBlank()) {
            log.warn("Empty JSON for class: {}", clazz.getSimpleName());
            return clazz.getDeclaredConstructor().newInstance(); // return empty object
        }

        return objectMapper.readValue(json, clazz);
    }


    private void validateData(DataDTO data, String messageType) {

        if (data.getAlert_config() == null) {
            throw new RuntimeException("alert_config is missing");
        }

        if (data.getHeaders() == null) {
            throw new RuntimeException("headers is missing");
        }

        if (data.getRaw_content() == null) {
            throw new RuntimeException("raw_content is missing");
        }

        if (data.getIndexed_content() == null) {
            throw new RuntimeException("indexed_content is missing");
        }

        if (data.getParam_mapping() == null) {
            throw new RuntimeException("param_mapping is missing");
        }

        // MESSAGE TYPE SPECIFIC VALIDATION

        if ("EMAIL".equalsIgnoreCase(messageType)) {

            if (data.getHeaders().getHeaders_email() == null) {
                throw new RuntimeException("EMAIL headers missing");
            }

            if (data.getRaw_content().getRawcontent_email() == null) {
                throw new RuntimeException("EMAIL raw content missing");
            }

            if (data.getIndexed_content().getIndexed_content_email() == null) {
                throw new RuntimeException("EMAIL indexed content missing");
            }

            if (data.getParam_mapping().getParam_mapping_email() == null) {
                throw new RuntimeException("EMAIL param mapping missing");
            }
        }

        if ("SMS".equalsIgnoreCase(messageType)) {

            if (data.getHeaders().getHeaders_sms() == null) {
                throw new RuntimeException("SMS headers missing");
            }

            if (data.getRaw_content().getRawcontent_sms() == null) {
                throw new RuntimeException("SMS raw content missing");
            }

            if (data.getIndexed_content().getIndexed_content_sms() == null) {
                throw new RuntimeException("SMS indexed content missing");
            }

            if (data.getParam_mapping().getParam_mapping_sms() == null) {
                throw new RuntimeException("SMS param mapping missing");
            }
        }
    }
}