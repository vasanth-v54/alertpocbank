package com.notification.consumer.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.notification.consumer.entity.Channel;
import com.notification.consumer.entity.TemplateDraft;
import com.notification.consumer.entity.TemplateStatus;
import com.notification.consumer.repository.TemplateDraftRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TemplateDraftService {

    private final TemplateDraftRepository repository;

    public List<TemplateDraft> getActiveTemplatesByChannel(Channel channel) {
        return repository.findByChannelAndStatus(channel, TemplateStatus.ACTIVE);
    }
}
