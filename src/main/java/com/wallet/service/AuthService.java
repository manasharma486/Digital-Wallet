package com.wallet.service;

import com.wallet.dto.auth.AuthResponse;
import com.wallet.dto.auth.LoginRequest;
import com.wallet.dto.auth.RegisterRequest;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
}
