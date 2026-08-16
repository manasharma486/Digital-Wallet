package com.wallet.service.impl;

import com.wallet.dto.audit.AuditLogResponse;
import com.wallet.dto.PageResponse;
import com.wallet.entity.AuditLog;
import com.wallet.entity.User;
import com.wallet.enums.AuditAction;
import com.wallet.mapper.AuditLogMapper;
import com.wallet.repository.AuditLogRepository;
import com.wallet.service.AuditLogService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final AuditLogMapper auditLogMapper;

    public AuditLogServiceImpl(AuditLogRepository auditLogRepository, AuditLogMapper auditLogMapper) {
        this.auditLogRepository = auditLogRepository;
        this.auditLogMapper = auditLogMapper;
    }

    @Override
    @Transactional
    public void logAction(User user, AuditAction action, String entityType, Long entityId, String ipAddress, String description) {
        AuditLog auditLog = AuditLog.builder()
                .user(user)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .ipAddress(ipAddress != null ? ipAddress : "0.0.0.0")
                .description(description)
                .build();
        auditLogRepository.save(auditLog);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AuditLogResponse> getAuditLogs(Pageable pageable) {
        Page<AuditLogResponse> page = auditLogRepository.findAll(pageable)
                .map(auditLogMapper::toDto);
        return PageResponse.from(page);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AuditLogResponse> getUserAuditLogs(Long userId, Pageable pageable) {
        Page<AuditLogResponse> page = auditLogRepository.findByUserId(userId, pageable)
                .map(auditLogMapper::toDto);
        return PageResponse.from(page);
    }
}
