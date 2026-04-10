package com.notification.consumer.service;

import com.notification.consumer.entity.TemplateMaster;

import java.util.List;

public interface TemplateMasterService {

    List<TemplateMaster> getActiveTemplatesByMessageType(String messageType);

    TemplateMaster getTemplateByNameAndType(String templateName, String messageType);
    
    List<TemplateMaster> getAllActiveTemplates();
}