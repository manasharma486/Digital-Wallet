package com.wallet.service.impl;

import com.wallet.dto.PageResponse;
import com.wallet.dto.transaction.TransactionResponse;
import com.wallet.dto.transaction.TransferRequest;
import com.wallet.entity.Transaction;
import com.wallet.entity.User;
import com.wallet.entity.Wallet;
import com.wallet.enums.AuditAction;
import com.wallet.enums.TransactionStatus;
import com.wallet.enums.TransactionType;
import com.wallet.enums.WalletStatus;
import com.wallet.exception.*;
import com.wallet.mapper.TransactionMapper;
import com.wallet.repository.TransactionRepository;
import com.wallet.repository.UserRepository;
import com.wallet.repository.WalletRepository;
import com.wallet.service.AuditLogService;
import com.wallet.service.TransactionService;
import com.wallet.util.ReferenceGenerator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final TransactionMapper transactionMapper;
    private final AuditLogService auditLogService;

    public TransactionServiceImpl(TransactionRepository transactionRepository, UserRepository userRepository, WalletRepository walletRepository, TransactionMapper transactionMapper, AuditLogService auditLogService) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
        this.transactionMapper = transactionMapper;
        this.auditLogService = auditLogService;
    }

    @Override
    @Transactional
    public TransactionResponse transfer(String senderEmail, TransferRequest request, String idempotencyKey) {
        // 1. Idempotency Check
        if (StringUtils.hasText(idempotencyKey)) {
            var existingTxn = transactionRepository.findByIdempotencyKey(idempotencyKey);
            if (existingTxn.isPresent()) {
                return transactionMapper.toDto(existingTxn.get());
            }
        }

        // 2. Validate Amount
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionException("Transfer amount must be greater than zero");
        }

        // 3. Self-transfer Check
        if (senderEmail.equalsIgnoreCase(request.getReceiverEmail().trim())) {
            throw new InvalidTransactionException("Self-transfer is not allowed");
        }

        // 4. Find Sender
        User sender = userRepository.findByEmail(senderEmail)
                .orElseThrow(() -> new UserNotFoundException("Sender user not found: " + senderEmail));

        // 5. Find Receiver
        User receiver = userRepository.findByEmail(request.getReceiverEmail().trim().toLowerCase())
                .orElseThrow(() -> new UserNotFoundException("Receiver user not found with email: " + request.getReceiverEmail()));

        // 6. Find Sender Wallet
        Wallet senderWallet = walletRepository.findByUserId(sender.getId())
                .orElseThrow(() -> new WalletNotFoundException("Sender wallet not found"));

        // 7. Find Receiver Wallet
        Wallet receiverWallet = walletRepository.findByUserId(receiver.getId())
                .orElseThrow(() -> new WalletNotFoundException("Receiver wallet not found"));

        // 8. Check Sender Wallet Status
        if (senderWallet.getStatus() != WalletStatus.ACTIVE) {
            throw new WalletFrozenException("Sender wallet is not active. Status: " + senderWallet.getStatus());
        }

        // 9. Check Receiver Wallet Status
        if (receiverWallet.getStatus() != WalletStatus.ACTIVE) {
            throw new WalletFrozenException("Receiver wallet is not active. Status: " + receiverWallet.getStatus());
        }

        // 10. Check Sender Balance
        if (senderWallet.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientBalanceException("Insufficient wallet balance. Available: "
                    + senderWallet.getBalance() + ", Requested: " + request.getAmount());
        }

        // 11. Debit Sender
        senderWallet.setBalance(senderWallet.getBalance().subtract(request.getAmount()));
        walletRepository.save(senderWallet);

        // 12. Credit Receiver
        receiverWallet.setBalance(receiverWallet.getBalance().add(request.getAmount()));
        walletRepository.save(receiverWallet);

        // 13. Create Transaction Record
        Transaction transaction = Transaction.builder()
                .reference(ReferenceGenerator.generateTransactionReference())
                .sender(sender)
                .receiver(receiver)
                .amount(request.getAmount())
                .type(TransactionType.TRANSFER)
                .status(TransactionStatus.SUCCESS)
                .description(request.getDescription())
                .idempotencyKey(StringUtils.hasText(idempotencyKey) ? idempotencyKey : null)
                .build();

        transaction = transactionRepository.save(transaction);

        // 14. Audit Log
        auditLogService.logAction(sender, AuditAction.TRANSFER, "Transaction", transaction.getId(), null,
                "Transferred " + request.getAmount() + " to " + receiver.getEmail());

        return transactionMapper.toDto(transaction);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TransactionResponse> getUserTransactions(
            String userEmail,
            TransactionType type,
            TransactionStatus status,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            Pageable pageable) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userEmail));

        Page<TransactionResponse> page = transactionRepository
                .findUserTransactions(user.getId(), type, status, fromDate, toDate, pageable)
                .map(transactionMapper::toDto);

        return PageResponse.from(page);
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionResponse getTransactionById(Long transactionId, String userEmail, boolean isAdmin) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new InvalidTransactionException("Transaction not found with id: " + transactionId));

        if (!isAdmin) {
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new UserNotFoundException("User not found: " + userEmail));

            boolean isSender = transaction.getSender() != null && transaction.getSender().getId().equals(user.getId());
            boolean isReceiver = transaction.getReceiver() != null && transaction.getReceiver().getId().equals(user.getId());

            if (!isSender && !isReceiver) {
                throw new UnauthorizedTransactionException("You are not authorized to access this transaction");
            }
        }

        return transactionMapper.toDto(transaction);
    }
}
