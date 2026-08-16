package com.wallet.service.impl;

import com.wallet.dto.PageResponse;
import com.wallet.dto.transaction.TransactionResponse;
import com.wallet.dto.user.UserResponse;
import com.wallet.dto.wallet.WalletResponse;
import com.wallet.enums.TransactionStatus;
import com.wallet.enums.TransactionType;
import com.wallet.mapper.TransactionMapper;
import com.wallet.mapper.UserMapper;
import com.wallet.mapper.WalletMapper;
import com.wallet.repository.TransactionRepository;
import com.wallet.repository.UserRepository;
import com.wallet.repository.WalletRepository;
import com.wallet.service.AdminService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final UserMapper userMapper;
    private final WalletMapper walletMapper;
    private final TransactionMapper transactionMapper;

    public AdminServiceImpl(UserRepository userRepository, WalletRepository walletRepository, TransactionRepository transactionRepository, UserMapper userMapper, WalletMapper walletMapper, TransactionMapper transactionMapper) {
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
        this.userMapper = userMapper;
        this.walletMapper = walletMapper;
        this.transactionMapper = transactionMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserResponse> getAllUsers(Pageable pageable) {
        Page<UserResponse> page = userRepository.findAll(pageable).map(userMapper::toDto);
        return PageResponse.from(page);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<WalletResponse> getAllWallets(Pageable pageable) {
        Page<WalletResponse> page = walletRepository.findAll(pageable).map(walletMapper::toDto);
        return PageResponse.from(page);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TransactionResponse> getAllTransactions(
            TransactionType type,
            TransactionStatus status,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            Pageable pageable) {

        Page<TransactionResponse> page = transactionRepository
                .findAllTransactionsFiltered(type, status, fromDate, toDate, pageable)
                .map(transactionMapper::toDto);
        return PageResponse.from(page);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getSystemStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userRepository.count());
        stats.put("totalWallets", walletRepository.count());
        stats.put("totalTransactions", transactionRepository.count());
        return stats;
    }
}
