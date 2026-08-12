package com.wallet.service.impl;

import com.wallet.dto.auth.AuthResponse;
import com.wallet.dto.auth.LoginRequest;
import com.wallet.dto.auth.RegisterRequest;
import com.wallet.entity.User;
import com.wallet.entity.Wallet;
import com.wallet.enums.AuditAction;
import com.wallet.enums.Role;
import com.wallet.enums.WalletStatus;
import com.wallet.exception.UserAlreadyExistsException;
import com.wallet.mapper.UserMapper;
import com.wallet.repository.UserRepository;
import com.wallet.repository.WalletRepository;
import com.wallet.security.CustomUserDetails;
import com.wallet.security.JwtTokenProvider;
import com.wallet.service.AuditLogService;
import com.wallet.service.AuthService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserMapper userMapper;
    private final AuditLogService auditLogService;

    public AuthServiceImpl(UserRepository userRepository, WalletRepository walletRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, JwtTokenProvider tokenProvider, UserMapper userMapper, AuditLogService auditLogService) {
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.userMapper = userMapper;
        this.auditLogService = auditLogService;
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("User already exists with email: " + request.getEmail());
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail().toLowerCase().trim())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .build();

        user = userRepository.save(user);

        Wallet wallet = Wallet.builder()
                .user(user)
                .balance(BigDecimal.ZERO.setScale(4))
                .currency("INR")
                .status(WalletStatus.ACTIVE)
                .build();

        walletRepository.save(wallet);

        auditLogService.logAction(user, AuditAction.REGISTER, "User", user.getId(), null, "User registered successfully");

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        String token = tokenProvider.generateToken(authentication);

        return AuthResponse.builder()
                .token(token)
                .user(userMapper.toDto(user))
                .build();
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail().toLowerCase().trim(), request.getPassword())
        );

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();

        String token = tokenProvider.generateToken(authentication);

        auditLogService.logAction(user, AuditAction.LOGIN, "User", user.getId(), null, "User logged in successfully");

        return AuthResponse.builder()
                .token(token)
                .user(userMapper.toDto(user))
                .build();
    }
}
