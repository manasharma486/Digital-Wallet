package com.wallet.dto.transaction;

import com.wallet.enums.TransactionStatus;
import com.wallet.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionResponse {
    private Long id;
    private String reference;
    private String senderEmail;
    private String senderName;
    private String receiverEmail;
    private String receiverName;
    private BigDecimal amount;
    private TransactionType type;
    private TransactionStatus status;
    private String description;
    private String idempotencyKey;
    private LocalDateTime createdAt;

    public TransactionResponse() {}

    public TransactionResponse(Long id, String reference, String senderEmail, String senderName, String receiverEmail, String receiverName, BigDecimal amount, TransactionType type, TransactionStatus status, String description, String idempotencyKey, LocalDateTime createdAt) {
        this.id = id;
        this.reference = reference;
        this.senderEmail = senderEmail;
        this.senderName = senderName;
        this.receiverEmail = receiverEmail;
        this.receiverName = receiverName;
        this.amount = amount;
        this.type = type;
        this.status = status;
        this.description = description;
        this.idempotencyKey = idempotencyKey;
        this.createdAt = createdAt;
    }

    public static TransactionResponseBuilder builder() {
        return new TransactionResponseBuilder();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }

    public String getSenderEmail() { return senderEmail; }
    public void setSenderEmail(String senderEmail) { this.senderEmail = senderEmail; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public String getReceiverEmail() { return receiverEmail; }
    public void setReceiverEmail(String receiverEmail) { this.receiverEmail = receiverEmail; }

    public String getReceiverName() { return receiverName; }
    public void setReceiverName(String receiverName) { this.receiverName = receiverName; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public TransactionType getType() { return type; }
    public void setType(TransactionType type) { this.type = type; }

    public TransactionStatus getStatus() { return status; }
    public void setStatus(TransactionStatus status) { this.status = status; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public static class TransactionResponseBuilder {
        private Long id;
        private String reference;
        private String senderEmail;
        private String senderName;
        private String receiverEmail;
        private String receiverName;
        private BigDecimal amount;
        private TransactionType type;
        private TransactionStatus status;
        private String description;
        private String idempotencyKey;
        private LocalDateTime createdAt;

        public TransactionResponseBuilder id(Long id) { this.id = id; return this; }
        public TransactionResponseBuilder reference(String reference) { this.reference = reference; return this; }
        public TransactionResponseBuilder senderEmail(String senderEmail) { this.senderEmail = senderEmail; return this; }
        public TransactionResponseBuilder senderName(String senderName) { this.senderName = senderName; return this; }
        public TransactionResponseBuilder receiverEmail(String receiverEmail) { this.receiverEmail = receiverEmail; return this; }
        public TransactionResponseBuilder receiverName(String receiverName) { this.receiverName = receiverName; return this; }
        public TransactionResponseBuilder amount(BigDecimal amount) { this.amount = amount; return this; }
        public TransactionResponseBuilder type(TransactionType type) { this.type = type; return this; }
        public TransactionResponseBuilder status(TransactionStatus status) { this.status = status; return this; }
        public TransactionResponseBuilder description(String description) { this.description = description; return this; }
        public TransactionResponseBuilder idempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; return this; }
        public TransactionResponseBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public TransactionResponse build() {
            return new TransactionResponse(id, reference, senderEmail, senderName, receiverEmail, receiverName, amount, type, status, description, idempotencyKey, createdAt);
        }
    }
}
