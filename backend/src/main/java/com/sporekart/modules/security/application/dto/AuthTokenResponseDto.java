package com.sporekart.modules.security.application.dto;

public class AuthTokenResponseDto {

    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    private long expiresInMs;
    private String userId;
    private String email;
    private String role;
    private String sessionId;

    public AuthTokenResponseDto() {}

    public AuthTokenResponseDto(String accessToken, String refreshToken, long expiresInMs, String userId, String email, String role, String sessionId) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresInMs = expiresInMs;
        this.userId = userId;
        this.email = email;
        this.role = role;
        this.sessionId = sessionId;
    }

    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
    public String getTokenType() { return tokenType; }
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }
    public long getExpiresInMs() { return expiresInMs; }
    public void setExpiresInMs(long expiresInMs) { this.expiresInMs = expiresInMs; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
}
