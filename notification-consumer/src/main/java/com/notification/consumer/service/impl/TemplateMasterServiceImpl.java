package com.notification.consumer.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.notification.consumer.entity.TemplateMaster;
import com.notification.consumer.repository.TemplateMasterRepository;
import com.notification.consumer.service.TemplateMasterService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TemplateMasterServiceImpl implements TemplateMasterService {

    private final TemplateMasterRepository repository;
    
    public TemplateMasterServiceImpl(TemplateMasterRepository repository) {
		super();
		this.repository = repository;
	}

	@Override
    public List<TemplateMaster> getActiveTemplatesByMessageType(String messageType) {
        return repository.findByMessageTypeAndIsActive(messageType, 1);
    }

    @Override
    public TemplateMaster getTemplateByNameAndType(String templateName, String messageType) {

        List<TemplateMaster> templates =
                repository.findByMessageTypeAndIsActive(messageType, 1);

        return templates.stream()
                .filter(t -> t.getTemplateName().equalsIgnoreCase(templateName))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(
                        "Template not found for name: " + templateName + " and type: " + messageType));
    }
    
 // ✅ NEW METHOD
    @Override
    public List<TemplateMaster> getAllActiveTemplates() {
        return repository.findByIsActive(1);
    }
}