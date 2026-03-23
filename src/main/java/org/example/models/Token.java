package org.example.models;

import java.util.UUID;

public class Token {
    public static final long TOKEN_VALIDITY = 2 * 60 * 60 * 1000; // 2h

    private String tokenId;
    private String userId;
    private String role;
    private Long issuedAt;
    private Long expiresAt;

    public Token() {}

    public Token(String userId, String role) {
        this.tokenId = UUID.randomUUID().toString();
        this.userId = userId;
        this.role = role;
        this.issuedAt = System.currentTimeMillis();
        this.expiresAt = System.currentTimeMillis() + TOKEN_VALIDITY;
    }

    public String getTokenId() {
        return tokenId;
    }

    public void setTokenId(String tokenId) {
        this.tokenId = tokenId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Long getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(Long issuedAt) {
        this.issuedAt = issuedAt;
    }

    public Long getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Long expiresAt) {
        this.expiresAt = expiresAt;
    }
}