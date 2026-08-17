package com.wallet.service;

import com.wallet.dto.transaction.TransactionResponse;
import com.wallet.dto.transaction.TransferRequest;
import com.wallet.entity.Transaction;
import com.wallet.entity.User;
import com.wallet.entity.Wallet;
import com.wallet.enums.Role;
import com.wallet.enums.TransactionStatus;
import com.wallet.enums.TransactionType;
import com.wallet.enums.WalletStatus;
import com.wallet.exception.InsufficientBalanceException;
import com.wallet.exception.InvalidTransactionException;
import com.wallet.mapper.TransactionMapper;
import com.wallet.repository.TransactionRepository;
import com.wallet.repository.UserRepository;
import com.wallet.repository.WalletRepository;
import com.wallet.service.impl.TransactionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private WalletRepository walletRepository;
    @Mock
    private AuditLogService auditLogService;

    private TransactionMapper transactionMapper;
    private TransactionServiceImpl transactionService;

    private User sender;
    private User receiver;
    private Wallet senderWallet;
    private Wallet receiverWallet;

    @BeforeEach
    void setUp() {
        transactionMapper = new TransactionMapper();
        transactionService = new TransactionServiceImpl(transactionRepository, userRepository, walletRepository, transactionMapper, auditLogService);

        sender = User.builder().id(1L).email("sender@example.com").fullName("Sender").role(Role.USER).build();
        receiver = User.builder().id(2L).email("receiver@example.com").fullName("Receiver").role(Role.USER).build();

        senderWallet = Wallet.builder().id(10L).user(sender).balance(new BigDecimal("1000.0000")).currency("INR").status(WalletStatus.ACTIVE).version(0L).build();
        receiverWallet = Wallet.builder().id(20L).user(receiver).balance(new BigDecimal("500.0000")).currency("INR").status(WalletStatus.ACTIVE).version(0L).build();
    }

    @Test
    @DisplayName("Transfer should debit sender, credit receiver, and create transaction record")
    void transfer_Success() {
        TransferRequest request = TransferRequest.builder()
                .receiverEmail("receiver@example.com")
                .amount(new BigDecimal("300.00"))
                .description("Lunch share")
                .build();

        when(userRepository.findByEmail("sender@example.com")).thenReturn(Optional.of(sender));
        when(userRepository.findByEmail("receiver@example.com")).thenReturn(Optional.of(receiver));
        when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(senderWallet));
        when(walletRepository.findByUserId(2L)).thenReturn(Optional.of(receiverWallet));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArgument(0));

        transactionService.transfer("sender@example.com", request, null);

        assertEquals(new BigDecimal("700.0000"), senderWallet.getBalance());
        assertEquals(new BigDecimal("800.0000"), receiverWallet.getBalance());
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    @DisplayName("Self transfer should throw InvalidTransactionException")
    void transfer_SelfTransfer_ThrowsException() {
        TransferRequest request = TransferRequest.builder()
                .receiverEmail("sender@example.com")
                .amount(new BigDecimal("100.00"))
                .build();

        assertThrows(InvalidTransactionException.class, () ->
                transactionService.transfer("sender@example.com", request, null));
    }

    @Test
    @DisplayName("Transfer with insufficient balance should throw InsufficientBalanceException")
    void transfer_InsufficientBalance_ThrowsException() {
        TransferRequest request = TransferRequest.builder()
                .receiverEmail("receiver@example.com")
                .amount(new BigDecimal("5000.00"))
                .build();

        when(userRepository.findByEmail("sender@example.com")).thenReturn(Optional.of(sender));
        when(userRepository.findByEmail("receiver@example.com")).thenReturn(Optional.of(receiver));
        when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(senderWallet));
        when(walletRepository.findByUserId(2L)).thenReturn(Optional.of(receiverWallet));

        assertThrows(InsufficientBalanceException.class, () ->
                transactionService.transfer("sender@example.com", request, null));
    }

    @Test
    @DisplayName("Transfer with duplicate idempotency key should return existing transaction without modifying balances")
    void transfer_IdempotencyDuplicate_ReturnsExistingTransaction() {
        String key = "idempotency-key-abc-123";
        TransferRequest request = TransferRequest.builder()
                .receiverEmail("receiver@example.com")
                .amount(new BigDecimal("300.00"))
                .build();

        Transaction existingTxn = Transaction.builder()
                .id(99L)
                .reference("TXN-20260819-EXISTING")
                .amount(new BigDecimal("300.00"))
                .idempotencyKey(key)
                .status(TransactionStatus.SUCCESS)
                .type(TransactionType.TRANSFER)
                .build();

        when(transactionRepository.findByIdempotencyKey(key)).thenReturn(Optional.of(existingTxn));

        TransactionResponse response = transactionService.transfer("sender@example.com", request, key);

        assertNotNull(response);
        assertEquals("TXN-20260819-EXISTING", response.getReference());
        verify(walletRepository, never()).save(any());
    }
}
