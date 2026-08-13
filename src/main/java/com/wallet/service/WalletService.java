package com.wallet.service;

import com.wallet.dto.wallet.WalletResponse;

import java.math.BigDecimal;

public interface WalletService {
    WalletResponse getWalletByUserEmail(String email);
    WalletResponse deposit(String userEmail, BigDecimal amount, String idempotencyKey);
    WalletResponse withdraw(String userEmail, BigDecimal amount, String idempotencyKey);
    WalletResponse freezeWallet(Long walletId, String adminEmail);
    WalletResponse unfreezeWallet(Long walletId, String adminEmail);
}
