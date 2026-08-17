package com.wallet.integration;

import com.wallet.dto.transaction.TransferRequest;
import com.wallet.entity.User;
import com.wallet.entity.Wallet;
import com.wallet.enums.Role;
import com.wallet.enums.WalletStatus;
import com.wallet.exception.InsufficientBalanceException;
import com.wallet.repository.TransactionRepository;
import com.wallet.repository.UserRepository;
import com.wallet.repository.WalletRepository;
import com.wallet.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class MoneyTransferIntegrationTest {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    private User alice;
    private User bob;

    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();
        walletRepository.deleteAll();
        userRepository.deleteAll();

        alice = User.builder()
                .fullName("Alice Smith")
                .email("alice@example.com")
                .password("password")
                .role(Role.USER)
                .build();
        alice = userRepository.save(alice);

        Wallet aliceWallet = Wallet.builder()
                .user(alice)
                .balance(new BigDecimal("1000.0000"))
                .currency("INR")
                .status(WalletStatus.ACTIVE)
                .build();
        walletRepository.save(aliceWallet);

        bob = User.builder()
                .fullName("Bob Jones")
                .email("bob@example.com")
                .password("password")
                .role(Role.USER)
                .build();
        bob = userRepository.save(bob);

        Wallet bobWallet = Wallet.builder()
                .user(bob)
                .balance(new BigDecimal("500.0000"))
                .currency("INR")
                .status(WalletStatus.ACTIVE)
                .build();
        walletRepository.save(bobWallet);
    }

    @Test
    @DisplayName("Integration: Successful money transfer between two active wallets")
    void testSuccessfulMoneyTransfer() {
        TransferRequest request = TransferRequest.builder()
                .receiverEmail("bob@example.com")
                .amount(new BigDecimal("400.00"))
                .description("Rent payment")
                .build();

        var response = transactionService.transfer("alice@example.com", request, "idemp-001");

        assertNotNull(response);
        assertNotNull(response.getReference());

        Wallet aliceWalletUpdated = walletRepository.findByUserId(alice.getId()).orElseThrow();
        Wallet bobWalletUpdated = walletRepository.findByUserId(bob.getId()).orElseThrow();

        assertEquals(0, new BigDecimal("600.0000").compareTo(aliceWalletUpdated.getBalance()));
        assertEquals(0, new BigDecimal("900.0000").compareTo(bobWalletUpdated.getBalance()));
    }

    @Test
    @DisplayName("Integration: Failed transfer due to insufficient balance rolls back completely")
    void testInsufficientBalanceRollback() {
        TransferRequest request = TransferRequest.builder()
                .receiverEmail("bob@example.com")
                .amount(new BigDecimal("2000.00"))
                .description("Too expensive")
                .build();

        assertThrows(InsufficientBalanceException.class, () ->
                transactionService.transfer("alice@example.com", request, "idemp-002"));

        Wallet aliceWalletUpdated = walletRepository.findByUserId(alice.getId()).orElseThrow();
        Wallet bobWalletUpdated = walletRepository.findByUserId(bob.getId()).orElseThrow();

        assertEquals(0, new BigDecimal("1000.0000").compareTo(aliceWalletUpdated.getBalance()));
        assertEquals(0, new BigDecimal("500.0000").compareTo(bobWalletUpdated.getBalance()));
    }
}
