package com.wallet.dto.audit;

import com.wallet.enums.AuditAction;

import java.time.LocalDateTime;

public class AuditLogResponse {
    private Long id;
    private String userEmail;
    private AuditAction action;
    private String entityType;
    private Long entityId;
    private String ipAddress;
    private String description;
    private LocalDateTime timestamp;

    public AuditLogResponse() {}

    public AuditLogResponse(Long id, String userEmail, AuditAction action, String entityType, Long entityId, String ipAddress, String description, LocalDateTime timestamp) {
        this.id = id;
        this.userEmail = userEmail;
        this.action = action;
        this.entityType = entityType;
        this.entityId = entityId;
        this.ipAddress = ipAddress;
        this.description = description;
        this.timestamp = timestamp;
    }

    public static AuditLogResponseBuilder builder() {
        return new AuditLogResponseBuilder();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public AuditAction getAction() { return action; }
    public void setAction(AuditAction action) { this.action = action; }

    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }

    public Long getEntityId() { return entityId; }
    public void setEntityId(Long entityId) { this.entityId = entityId; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public static class AuditLogResponseBuilder {
        private Long id;
        private String userEmail;
        private AuditAction action;
        private String entityType;
        private Long entityId;
        private String ipAddress;
        private String description;
        private LocalDateTime timestamp;

        public AuditLogResponseBuilder id(Long id) { this.id = id; return this; }
        public AuditLogResponseBuilder userEmail(String userEmail) { this.userEmail = userEmail; return this; }
        public AuditLogResponseBuilder action(AuditAction action) { this.action = action; return this; }
        public AuditLogResponseBuilder entityType(String entityType) { this.entityType = entityType; return this; }
        public AuditLogResponseBuilder entityId(Long entityId) { this.entityId = entityId; return this; }
        public AuditLogResponseBuilder ipAddress(String ipAddress) { this.ipAddress = ipAddress; return this; }
        public AuditLogResponseBuilder description(String description) { this.description = description; return this; }
        public AuditLogResponseBuilder timestamp(LocalDateTime timestamp) { this.timestamp = timestamp; return this; }

        public AuditLogResponse build() {
            return new AuditLogResponse(id, userEmail, action, entityType, entityId, ipAddress, description, timestamp);
        }
    }
}
