package com.wallet.dto.wallet;

import com.wallet.enums.WalletStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class WalletResponse {
    private Long id;
    private Long userId;
    private String userEmail;
    private String userFullName;
    private BigDecimal balance;
    private String currency;
    private WalletStatus status;
    private Long version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public WalletResponse() {}

    public WalletResponse(Long id, Long userId, String userEmail, String userFullName, BigDecimal balance, String currency, WalletStatus status, Long version, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.userEmail = userEmail;
        this.userFullName = userFullName;
        this.balance = balance;
        this.currency = currency;
        this.status = status;
        this.version = version;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static WalletResponseBuilder builder() {
        return new WalletResponseBuilder();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getUserFullName() { return userFullName; }
    public void setUserFullName(String userFullName) { this.userFullName = userFullName; }

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

    public static class WalletResponseBuilder {
        private Long id;
        private Long userId;
        private String userEmail;
        private String userFullName;
        private BigDecimal balance;
        private String currency;
        private WalletStatus status;
        private Long version;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public WalletResponseBuilder id(Long id) { this.id = id; return this; }
        public WalletResponseBuilder userId(Long userId) { this.userId = userId; return this; }
        public WalletResponseBuilder userEmail(String userEmail) { this.userEmail = userEmail; return this; }
        public WalletResponseBuilder userFullName(String userFullName) { this.userFullName = userFullName; return this; }
        public WalletResponseBuilder balance(BigDecimal balance) { this.balance = balance; return this; }
        public WalletResponseBuilder currency(String currency) { this.currency = currency; return this; }
        public WalletResponseBuilder status(WalletStatus status) { this.status = status; return this; }
        public WalletResponseBuilder version(Long version) { this.version = version; return this; }
        public WalletResponseBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public WalletResponseBuilder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public WalletResponse build() {
            return new WalletResponse(id, userId, userEmail, userFullName, balance, currency, status, version, createdAt, updatedAt);
        }
    }
}
