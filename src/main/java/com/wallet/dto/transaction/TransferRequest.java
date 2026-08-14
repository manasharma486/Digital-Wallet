package com.wallet.dto.transaction;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class TransferRequest {

    @NotBlank(message = "Receiver email is required")
    @Email(message = "Invalid receiver email format")
    private String receiverEmail;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Transfer amount must be greater than 0")
    private BigDecimal amount;

    private String description;

    public TransferRequest() {}

    public TransferRequest(String receiverEmail, BigDecimal amount, String description) {
        this.receiverEmail = receiverEmail;
        this.amount = amount;
        this.description = description;
    }

    public static TransferRequestBuilder builder() {
        return new TransferRequestBuilder();
    }

    public String getReceiverEmail() { return receiverEmail; }
    public void setReceiverEmail(String receiverEmail) { this.receiverEmail = receiverEmail; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public static class TransferRequestBuilder {
        private String receiverEmail;
        private BigDecimal amount;
        private String description;

        public TransferRequestBuilder receiverEmail(String receiverEmail) { this.receiverEmail = receiverEmail; return this; }
        public TransferRequestBuilder amount(BigDecimal amount) { this.amount = amount; return this; }
        public TransferRequestBuilder description(String description) { this.description = description; return this; }

        public TransferRequest build() {
            return new TransferRequest(receiverEmail, amount, description);
        }
    }
}
