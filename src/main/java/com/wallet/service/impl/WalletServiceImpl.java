package com.wallet.service.impl;

import com.wallet.dto.wallet.WalletResponse;
import com.wallet.entity.Transaction;
import com.wallet.entity.User;
import com.wallet.entity.Wallet;
import com.wallet.enums.AuditAction;
import com.wallet.enums.TransactionStatus;
import com.wallet.enums.TransactionType;
import com.wallet.enums.WalletStatus;
import com.wallet.exception.*;
import com.wallet.mapper.WalletMapper;
import com.wallet.repository.TransactionRepository;
import com.wallet.repository.UserRepository;
import com.wallet.repository.WalletRepository;
import com.wallet.service.AuditLogService;
import com.wallet.service.WalletService;
import com.wallet.util.ReferenceGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;

@Service
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final WalletMapper walletMapper;
    private final AuditLogService auditLogService;

    public WalletServiceImpl(WalletRepository walletRepository, UserRepository userRepository, TransactionRepository transactionRepository, WalletMapper walletMapper, AuditLogService auditLogService) {
        this.walletRepository = walletRepository;
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
        this.walletMapper = walletMapper;
        this.auditLogService = auditLogService;
    }

    @Override
    @Transactional(readOnly = true)
    public WalletResponse getWalletByUserEmail(String email) {
        Wallet wallet = walletRepository.findByUserEmail(email)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found for user email: " + email));
        return walletMapper.toDto(wallet);
    }

    @Override
    @Transactional
    public WalletResponse deposit(String userEmail, BigDecimal amount, String idempotencyKey) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionException("Deposit amount must be greater than zero");
        }

        if (StringUtils.hasText(idempotencyKey)) {
            var existingTxn = transactionRepository.findByIdempotencyKey(idempotencyKey);
            if (existingTxn.isPresent()) {
                Wallet wallet = walletRepository.findByUserEmail(userEmail)
                        .orElseThrow(() -> new WalletNotFoundException("Wallet not found"));
                return walletMapper.toDto(wallet);
            }
        }

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userEmail));

        Wallet wallet = walletRepository.findByUserId(user.getId())
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found for user: " + userEmail));

        if (wallet.getStatus() != WalletStatus.ACTIVE) {
            throw new WalletFrozenException("Cannot deposit to a non-active wallet. Current status: " + wallet.getStatus());
        }

        wallet.setBalance(wallet.getBalance().add(amount));
        wallet = walletRepository.save(wallet);

        Transaction transaction = Transaction.builder()
                .reference(ReferenceGenerator.generateTransactionReference())
                .receiver(user)
                .amount(amount)
                .type(TransactionType.DEPOSIT)
                .status(TransactionStatus.SUCCESS)
                .description("Deposit to wallet")
                .idempotencyKey(StringUtils.hasText(idempotencyKey) ? idempotencyKey : null)
                .build();

        transactionRepository.save(transaction);

        auditLogService.logAction(user, AuditAction.DEPOSIT, "Wallet", wallet.getId(), null,
                "Deposited amount: " + amount + " " + wallet.getCurrency());

        return walletMapper.toDto(wallet);
    }

    @Override
    @Transactional
    public WalletResponse withdraw(String userEmail, BigDecimal amount, String idempotencyKey) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionException("Withdrawal amount must be greater than zero");
        }

        if (StringUtils.hasText(idempotencyKey)) {
            var existingTxn = transactionRepository.findByIdempotencyKey(idempotencyKey);
            if (existingTxn.isPresent()) {
                Wallet wallet = walletRepository.findByUserEmail(userEmail)
                        .orElseThrow(() -> new WalletNotFoundException("Wallet not found"));
                return walletMapper.toDto(wallet);
            }
        }

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userEmail));

        Wallet wallet = walletRepository.findByUserId(user.getId())
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found for user: " + userEmail));

        if (wallet.getStatus() != WalletStatus.ACTIVE) {
            throw new WalletFrozenException("Cannot withdraw from a non-active wallet. Current status: " + wallet.getStatus());
        }

        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Insufficient wallet balance. Current balance: " + wallet.getBalance() + ", requested: " + amount);
        }

        wallet.setBalance(wallet.getBalance().subtract(amount));
        wallet = walletRepository.save(wallet);

        Transaction transaction = Transaction.builder()
                .reference(ReferenceGenerator.generateTransactionReference())
                .sender(user)
                .amount(amount)
                .type(TransactionType.WITHDRAWAL)
                .status(TransactionStatus.SUCCESS)
                .description("Withdrawal from wallet")
                .idempotencyKey(StringUtils.hasText(idempotencyKey) ? idempotencyKey : null)
                .build();

        transactionRepository.save(transaction);

        auditLogService.logAction(user, AuditAction.WITHDRAWAL, "Wallet", wallet.getId(), null,
                "Withdrew amount: " + amount + " " + wallet.getCurrency());

        return walletMapper.toDto(wallet);
    }

    @Override
    @Transactional
    public WalletResponse freezeWallet(Long walletId, String adminEmail) {
        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new UserNotFoundException("Admin not found: " + adminEmail));

        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found with id: " + walletId));

        wallet.setStatus(WalletStatus.FROZEN);
        wallet = walletRepository.save(wallet);

        auditLogService.logAction(admin, AuditAction.WALLET_FROZEN, "Wallet", wallet.getId(), null,
                "Wallet ID " + walletId + " frozen by admin " + adminEmail);

        return walletMapper.toDto(wallet);
    }

    @Override
    @Transactional
    public WalletResponse unfreezeWallet(Long walletId, String adminEmail) {
        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new UserNotFoundException("Admin not found: " + adminEmail));

        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found with id: " + walletId));

        wallet.setStatus(WalletStatus.ACTIVE);
        wallet = walletRepository.save(wallet);

        auditLogService.logAction(admin, AuditAction.WALLET_UNFROZEN, "Wallet", wallet.getId(), null,
                "Wallet ID " + walletId + " unfrozen by admin " + adminEmail);

        return walletMapper.toDto(wallet);
    }
}
