package com.wallet.service;

import com.wallet.entity.User;
import com.wallet.entity.Wallet;
import com.wallet.enums.Role;
import com.wallet.enums.WalletStatus;
import com.wallet.exception.InsufficientBalanceException;
import com.wallet.exception.WalletFrozenException;
import com.wallet.mapper.WalletMapper;
import com.wallet.repository.TransactionRepository;
import com.wallet.repository.UserRepository;
import com.wallet.repository.WalletRepository;
import com.wallet.service.impl.WalletServiceImpl;
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
class WalletServiceTest {

    @Mock
    private WalletRepository walletRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private AuditLogService auditLogService;

    private WalletMapper walletMapper;
    private WalletServiceImpl walletService;

    private User user;
    private Wallet activeWallet;

    @BeforeEach
    void setUp() {
        walletMapper = new WalletMapper();
        walletService = new WalletServiceImpl(walletRepository, userRepository, transactionRepository, walletMapper, auditLogService);

        user = User.builder()
                .id(1L)
                .email("user@example.com")
                .fullName("Test User")
                .role(Role.USER)
                .build();

        activeWallet = Wallet.builder()
                .id(10L)
                .user(user)
                .balance(new BigDecimal("1000.0000"))
                .currency("INR")
                .status(WalletStatus.ACTIVE)
                .version(0L)
                .build();
    }

    @Test
    @DisplayName("Deposit should increase wallet balance")
    void deposit_Success() {
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(activeWallet));
        when(walletRepository.save(any(Wallet.class))).thenAnswer(i -> i.getArgument(0));

        walletService.deposit("user@example.com", new BigDecimal("500.00"), null);

        assertEquals(new BigDecimal("1500.0000"), activeWallet.getBalance());
        verify(transactionRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Deposit to frozen wallet should throw WalletFrozenException")
    void deposit_FrozenWallet_ThrowsException() {
        activeWallet.setStatus(WalletStatus.FROZEN);
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(activeWallet));

        assertThrows(WalletFrozenException.class, () ->
                walletService.deposit("user@example.com", new BigDecimal("500.00"), null));
    }

    @Test
    @DisplayName("Withdraw should decrease wallet balance when sufficient funds exist")
    void withdraw_Success() {
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(activeWallet));
        when(walletRepository.save(any(Wallet.class))).thenAnswer(i -> i.getArgument(0));

        walletService.withdraw("user@example.com", new BigDecimal("300.00"), null);

        assertEquals(new BigDecimal("700.0000"), activeWallet.getBalance());
    }

    @Test
    @DisplayName("Withdraw should throw InsufficientBalanceException when requested amount exceeds balance")
    void withdraw_InsufficientBalance_ThrowsException() {
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(activeWallet));

        assertThrows(InsufficientBalanceException.class, () ->
                walletService.withdraw("user@example.com", new BigDecimal("2000.00"), null));
    }
}
