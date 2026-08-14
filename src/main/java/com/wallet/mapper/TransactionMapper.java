package com.wallet.mapper;

import com.wallet.dto.transaction.TransactionResponse;
import com.wallet.entity.Transaction;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {

    public TransactionResponse toDto(Transaction transaction) {
        if (transaction == null) {
            return null;
        }
        return TransactionResponse.builder()
                .id(transaction.getId())
                .reference(transaction.getReference())
                .senderEmail(transaction.getSender() != null ? transaction.getSender().getEmail() : null)
                .senderName(transaction.getSender() != null ? transaction.getSender().getFullName() : null)
                .receiverEmail(transaction.getReceiver() != null ? transaction.getReceiver().getEmail() : null)
                .receiverName(transaction.getReceiver() != null ? transaction.getReceiver().getFullName() : null)
                .amount(transaction.getAmount())
                .type(transaction.getType())
                .status(transaction.getStatus())
                .description(transaction.getDescription())
                .idempotencyKey(transaction.getIdempotencyKey())
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}
