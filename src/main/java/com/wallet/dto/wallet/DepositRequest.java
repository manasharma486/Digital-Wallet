package com.wallet.dto.wallet;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class DepositRequest {

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Deposit amount must be greater than 0")
    private BigDecimal amount;

    public DepositRequest() {}

    public DepositRequest(BigDecimal amount) {
        this.amount = amount;
    }

    public static DepositRequestBuilder builder() {
        return new DepositRequestBuilder();
    }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public static class DepositRequestBuilder {
        private BigDecimal amount;

        public DepositRequestBuilder amount(BigDecimal amount) { this.amount = amount; return this; }

        public DepositRequest build() {
            return new DepositRequest(amount);
        }
    }
}
