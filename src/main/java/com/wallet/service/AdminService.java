package com.wallet.service;

import com.wallet.dto.PageResponse;
import com.wallet.dto.transaction.TransactionResponse;
import com.wallet.dto.user.UserResponse;
import com.wallet.dto.wallet.WalletResponse;
import com.wallet.enums.TransactionStatus;
import com.wallet.enums.TransactionType;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Map;

public interface AdminService {
    PageResponse<UserResponse> getAllUsers(Pageable pageable);
    PageResponse<WalletResponse> getAllWallets(Pageable pageable);
    PageResponse<TransactionResponse> getAllTransactions(
            TransactionType type,
            TransactionStatus status,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            Pageable pageable
    );
    Map<String, Object> getSystemStats();
}
