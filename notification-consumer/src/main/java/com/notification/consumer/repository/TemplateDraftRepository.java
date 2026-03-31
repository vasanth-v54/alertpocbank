package com.notification.consumer.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.notification.consumer.entity.Channel;
import com.notification.consumer.entity.TemplateDraft;
import com.notification.consumer.entity.TemplateStatus;

@Repository
public interface TemplateDraftRepository extends JpaRepository<TemplateDraft, String> {

    List<TemplateDraft> findByChannelAndStatus(Channel channel, TemplateStatus status);

}