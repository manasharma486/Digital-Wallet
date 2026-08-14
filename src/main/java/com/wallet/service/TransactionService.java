package com.wallet.service;

import com.wallet.dto.PageResponse;
import com.wallet.dto.transaction.TransactionResponse;
import com.wallet.dto.transaction.TransferRequest;
import com.wallet.enums.TransactionStatus;
import com.wallet.enums.TransactionType;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface TransactionService {
    TransactionResponse transfer(String senderEmail, TransferRequest request, String idempotencyKey);

    PageResponse<TransactionResponse> getUserTransactions(
            String userEmail,
            TransactionType type,
            TransactionStatus status,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            Pageable pageable
    );

    TransactionResponse getTransactionById(Long transactionId, String userEmail, boolean isAdmin);
}
