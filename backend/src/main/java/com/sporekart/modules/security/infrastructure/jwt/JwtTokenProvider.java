package com.sporekart.modules.security.infrastructure.jwt;

import com.sporekart.modules.security.domain.UserRole;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);

    private final SecretKey key;
    private final long expirationMs;
    private final String issuer;

    public JwtTokenProvider(
            @Value("${app.security.jwt.secret:}") String secret,
            @Value("${app.security.jwt.expiration-ms:900000}") long expirationMs,
            @Value("${app.security.jwt.issuer:sporekart-platform}") String issuer
    ) {
        if (secret.length() < 32) {
            throw new IllegalArgumentException("JWT secret key must be at least 32 characters (256 bits) for production security");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
        this.issuer = issuer;
    }

    public String generateAccessToken(String userId, String email, UserRole role, String sessionId) {
        Instant now = Instant.now();
        Instant expiry = now.plusMillis(expirationMs);

        return Jwts.builder()
                .subject(userId)
                .claim("email", email)
                .claim("role", role.name())
                .claim("sessionId", sessionId)
                .issuer(issuer)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(key, Jwts.SIG.HS512)
                .compact();
    }

    public Claims parseAndValidateToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .requireIssuer(issuer)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            log.warn("JWT token is expired: {}", e.getMessage());
            throw e;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            throw new IllegalArgumentException("Invalid or tampered JWT token", e);
        }
    }

    public boolean validateToken(String token) {
        try {
            parseAndValidateToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String getUserIdFromToken(String token) {
        return parseAndValidateToken(token).getSubject();
    }

    public String getSessionIdFromToken(String token) {
        return parseAndValidateToken(token).get("sessionId", String.class);
    }

    public String getEmailFromToken(String token) {
        return parseAndValidateToken(token).get("email", String.class);
    }

    public String getRoleFromToken(String token) {
        return parseAndValidateToken(token).get("role", String.class);
    }
}
