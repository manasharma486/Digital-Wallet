package com.wallet.dto.wallet;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class WithdrawRequest {

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Withdrawal amount must be greater than 0")
    private BigDecimal amount;

    public WithdrawRequest() {}

    public WithdrawRequest(BigDecimal amount) {
        this.amount = amount;
    }

    public static WithdrawRequestBuilder builder() {
        return new WithdrawRequestBuilder();
    }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public static class WithdrawRequestBuilder {
        private BigDecimal amount;

        public WithdrawRequestBuilder amount(BigDecimal amount) { this.amount = amount; return this; }

        public WithdrawRequest build() {
            return new WithdrawRequest(amount);
        }
    }
}
