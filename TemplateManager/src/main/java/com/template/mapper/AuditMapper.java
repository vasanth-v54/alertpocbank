package com.template.mapper;

import com.template.api.model.TemplateAuditDto;
import com.template.entity.TemplateAudit;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID; // Import UUID
import java.util.stream.Collectors;

@Component
public class AuditMapper {

    public TemplateAuditDto toDto(TemplateAudit entity) {
        if (entity == null) {
            return null;
        }

        TemplateAuditDto dto = new TemplateAuditDto();
        
        // Convert String to UUID before setting
        if (entity.getAuditId() != null) {
            dto.setAuditId(UUID.fromString(entity.getAuditId()));
        }

        dto.setTemplateId(entity.getTemplateId());
        dto.setTemplateName(entity.getTemplateName());
        dto.setVersion(entity.getVersion());
        dto.setChannel(TemplateAuditDto.ChannelEnum.fromValue(entity.getChannel().name()));
        dto.setIsTemplateActive(TemplateAuditDto.IsTemplateActiveEnum.fromValue(entity.getIsTemplateActive().name()));
        dto.setChangedBy(entity.getChangedBy());
        dto.setChangedAt(entity.getChangedAt().atOffset(java.time.ZoneOffset.UTC));
        dto.setChangeReason(TemplateAuditDto.ChangeReasonEnum.fromValue(entity.getChangeReason().name()));

        return dto;
    }

    public List<TemplateAuditDto> toDto(List<TemplateAudit> entities) {
        return entities.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}
