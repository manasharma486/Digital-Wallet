package com.wallet.dto.auth;

import com.wallet.dto.user.UserResponse;

public class AuthResponse {
    private String token;
    private String tokenType = "Bearer";
    private UserResponse user;

    public AuthResponse() {}

    public AuthResponse(String token, String tokenType, UserResponse user) {
        this.token = token;
        this.tokenType = tokenType != null ? tokenType : "Bearer";
        this.user = user;
    }

    public static AuthResponseBuilder builder() {
        return new AuthResponseBuilder();
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getTokenType() { return tokenType; }
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }

    public UserResponse getUser() { return user; }
    public void setUser(UserResponse user) { this.user = user; }

    public static class AuthResponseBuilder {
        private String token;
        private String tokenType = "Bearer";
        private UserResponse user;

        public AuthResponseBuilder token(String token) { this.token = token; return this; }
        public AuthResponseBuilder tokenType(String tokenType) { this.tokenType = tokenType; return this; }
        public AuthResponseBuilder user(UserResponse user) { this.user = user; return this; }

        public AuthResponse build() {
            return new AuthResponse(token, tokenType, user);
        }
    }
}
