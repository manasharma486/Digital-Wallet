package com.wallet.mapper;

import com.wallet.dto.audit.AuditLogResponse;
import com.wallet.entity.AuditLog;
import org.springframework.stereotype.Component;

@Component
public class AuditLogMapper {

    public AuditLogResponse toDto(AuditLog auditLog) {
        if (auditLog == null) {
            return null;
        }
        return AuditLogResponse.builder()
                .id(auditLog.getId())
                .userEmail(auditLog.getUser() != null ? auditLog.getUser().getEmail() : null)
                .action(auditLog.getAction())
                .entityType(auditLog.getEntityType())
                .entityId(auditLog.getEntityId())
                .ipAddress(auditLog.getIpAddress())
                .description(auditLog.getDescription())
                .timestamp(auditLog.getTimestamp())
                .build();
    }
}
