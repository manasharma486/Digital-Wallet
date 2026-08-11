package com.wallet.entity;

import com.wallet.enums.WalletStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "wallets")
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal balance;

    @Column(nullable = false, length = 10)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private WalletStatus status;

    @Version
    private Long version;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Wallet() {}

    public Wallet(Long id, User user, BigDecimal balance, String currency, WalletStatus status, Long version, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.user = user;
        this.balance = balance;
        this.currency = currency;
        this.status = status;
        this.version = version;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static WalletBuilder builder() {
        return new WalletBuilder();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public WalletStatus getStatus() { return status; }
    public void setStatus(WalletStatus status) { this.status = status; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public static class WalletBuilder {
        private Long id;
        private User user;
        private BigDecimal balance;
        private String currency;
        private WalletStatus status;
        private Long version;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public WalletBuilder id(Long id) { this.id = id; return this; }
        public WalletBuilder user(User user) { this.user = user; return this; }
        public WalletBuilder balance(BigDecimal balance) { this.balance = balance; return this; }
        public WalletBuilder currency(String currency) { this.currency = currency; return this; }
        public WalletBuilder status(WalletStatus status) { this.status = status; return this; }
        public WalletBuilder version(Long version) { this.version = version; return this; }
        public WalletBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public WalletBuilder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public Wallet build() {
            return new Wallet(id, user, balance, currency, status, version, createdAt, updatedAt);
        }
    }
}
