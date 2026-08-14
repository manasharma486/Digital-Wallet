package com.wallet.entity;

import com.wallet.enums.TransactionStatus;
import com.wallet.enums.TransactionType;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions", indexes = {
        @Index(name = "idx_txn_sender_id", columnList = "sender_id"),
        @Index(name = "idx_txn_receiver_id", columnList = "receiver_id"),
        @Index(name = "idx_txn_status", columnList = "status"),
        @Index(name = "idx_txn_created_at", columnList = "created_at"),
        @Index(name = "idx_txn_reference", columnList = "reference"),
        @Index(name = "idx_txn_idempotency_key", columnList = "idempotency_key")
})
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String reference;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id")
    private User receiver;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionStatus status;

    @Column(length = 255)
    private String description;

    @Column(name = "idempotency_key", unique = true, length = 128)
    private String idempotencyKey;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Transaction() {}

    public Transaction(Long id, String reference, User sender, User receiver, BigDecimal amount, TransactionType type, TransactionStatus status, String description, String idempotencyKey, LocalDateTime createdAt) {
        this.id = id;
        this.reference = reference;
        this.sender = sender;
        this.receiver = receiver;
        this.amount = amount;
        this.type = type;
        this.status = status;
        this.description = description;
        this.idempotencyKey = idempotencyKey;
        this.createdAt = createdAt;
    }

    public static TransactionBuilder builder() {
        return new TransactionBuilder();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }

    public User getSender() { return sender; }
    public void setSender(User sender) { this.sender = sender; }

    public User getReceiver() { return receiver; }
    public void setReceiver(User receiver) { this.receiver = receiver; }

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

    public static class TransactionBuilder {
        private Long id;
        private String reference;
        private User sender;
        private User receiver;
        private BigDecimal amount;
        private TransactionType type;
        private TransactionStatus status;
        private String description;
        private String idempotencyKey;
        private LocalDateTime createdAt;

        public TransactionBuilder id(Long id) { this.id = id; return this; }
        public TransactionBuilder reference(String reference) { this.reference = reference; return this; }
        public TransactionBuilder sender(User sender) { this.sender = sender; return this; }
        public TransactionBuilder receiver(User receiver) { this.receiver = receiver; return this; }
        public TransactionBuilder amount(BigDecimal amount) { this.amount = amount; return this; }
        public TransactionBuilder type(TransactionType type) { this.type = type; return this; }
        public TransactionBuilder status(TransactionStatus status) { this.status = status; return this; }
        public TransactionBuilder description(String description) { this.description = description; return this; }
        public TransactionBuilder idempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; return this; }
        public TransactionBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public Transaction build() {
            return new Transaction(id, reference, sender, receiver, amount, type, status, description, idempotencyKey, createdAt);
        }
    }
}
