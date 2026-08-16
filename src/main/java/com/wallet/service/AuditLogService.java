package com.wallet.service;

import com.wallet.dto.audit.AuditLogResponse;
import com.wallet.dto.PageResponse;
import com.wallet.entity.User;
import com.wallet.enums.AuditAction;
import org.springframework.data.domain.Pageable;

public interface AuditLogService {
    void logAction(User user, AuditAction action, String entityType, Long entityId, String ipAddress, String description);
    PageResponse<AuditLogResponse> getAuditLogs(Pageable pageable);
    PageResponse<AuditLogResponse> getUserAuditLogs(Long userId, Pageable pageable);
}
