package com.sporekart.modules.security.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {

    @Id
    private String id;

    @Column(name = "session_id", nullable = false)
    private String sessionId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "token_hash", nullable = false, unique = true)
    private String tokenHash;

    @Column(name = "token_family", nullable = false)
    private String tokenFamily;

    @Column(name = "is_rotated", nullable = false)
    private boolean isRotated;

    @Column(name = "is_revoked", nullable = false)
    private boolean isRevoked;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected RefreshToken() {}

    public RefreshToken(String sessionId, String userId, String tokenHash, String tokenFamily, Instant expiresAt) {
        this.id = UUID.randomUUID().toString();
        this.sessionId = sessionId;
        this.userId = userId;
        this.tokenHash = tokenHash;
        this.tokenFamily = tokenFamily != null ? tokenFamily : UUID.randomUUID().toString();
        this.isRotated = false;
        this.isRevoked = false;
        this.expiresAt = expiresAt;
        this.createdAt = Instant.now();
    }

    public boolean isValid() {
        return !isRotated && !isRevoked && Instant.now().isBefore(expiresAt);
    }

    public void markRotated() {
        this.isRotated = true;
    }

    public void revoke() {
        this.isRevoked = true;
    }

    // Getters
    public String getId() { return id; }
    public String getSessionId() { return sessionId; }
    public String getUserId() { return userId; }
    public String getTokenHash() { return tokenHash; }
    public String getTokenFamily() { return tokenFamily; }
    public boolean isRotated() { return isRotated; }
    public boolean isRevoked() { return isRevoked; }
    public Instant getExpiresAt() { return expiresAt; }
    public Instant getCreatedAt() { return createdAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RefreshToken that = (RefreshToken) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
