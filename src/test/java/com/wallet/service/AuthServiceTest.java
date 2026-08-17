package com.wallet.service;

import com.wallet.dto.auth.AuthResponse;
import com.wallet.dto.auth.LoginRequest;
import com.wallet.dto.auth.RegisterRequest;
import com.wallet.entity.User;
import com.wallet.enums.Role;
import com.wallet.exception.UserAlreadyExistsException;
import com.wallet.mapper.UserMapper;
import com.wallet.repository.UserRepository;
import com.wallet.repository.WalletRepository;
import com.wallet.security.CustomUserDetails;
import com.wallet.security.JwtTokenProvider;
import com.wallet.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private WalletRepository walletRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private AuditLogService auditLogService;

    private UserMapper userMapper;
    private JwtTokenProvider tokenProvider;
    private AuthServiceImpl authService;
    private User sampleUser;

    @BeforeEach
    void setUp() {
        userMapper = new UserMapper();
        tokenProvider = new JwtTokenProvider("404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970", 86400000);
        authService = new AuthServiceImpl(userRepository, walletRepository, passwordEncoder, authenticationManager, tokenProvider, userMapper, auditLogService);

        sampleUser = User.builder()
                .id(1L)
                .fullName("John Doe")
                .email("john@example.com")
                .password("encoded_pass")
                .role(Role.USER)
                .build();
    }

    @Test
    @DisplayName("Register should successfully create user and wallet and return JWT token")
    void register_Success() {
        RegisterRequest request = RegisterRequest.builder()
                .fullName("John Doe")
                .email("john@example.com")
                .password("password123")
                .build();

        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded_pass");
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);

        Authentication authentication = mock(Authentication.class);
        CustomUserDetails userDetails = new CustomUserDetails(sampleUser);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertNotNull(response.getToken());
        verify(userRepository, times(1)).save(any(User.class));
        verify(walletRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Register should throw UserAlreadyExistsException when email exists")
    void register_DuplicateEmail_ThrowsException() {
        RegisterRequest request = RegisterRequest.builder()
                .fullName("John Doe")
                .email("john@example.com")
                .password("password123")
                .build();

        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Login should successfully authenticate user and return JWT token")
    void login_Success() {
        LoginRequest request = LoginRequest.builder()
                .email("john@example.com")
                .password("password123")
                .build();

        Authentication authentication = mock(Authentication.class);
        CustomUserDetails userDetails = new CustomUserDetails(sampleUser);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertNotNull(response.getToken());
    }
}
