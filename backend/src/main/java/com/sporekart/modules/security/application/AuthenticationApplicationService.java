package com.sporekart.modules.security.application;

import com.sporekart.modules.security.application.dto.*;
import com.sporekart.modules.security.domain.*;
import com.sporekart.modules.security.domain.exception.*;
import com.sporekart.modules.security.infrastructure.jwt.JwtTokenProvider;
import com.sporekart.modules.security.infrastructure.persistence.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthenticationApplicationService {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationApplicationService.class);

    private final UserAccountRepository userAccountRepository;
    private final UserSessionRepository userSessionRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final SecurityAuditEventRepository auditEventRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final LoginAttemptService loginAttemptService;
    private final SecurityAuditService securityAuditService;

    @Value("${app.security.refresh-token.expiration-days:7}")
    private long refreshTokenExpirationDays;

    @Value("${app.security.jwt.expiration-ms:900000}")
    private long accessTokenExpirationMs;

    public AuthenticationApplicationService(
            UserAccountRepository userAccountRepository,
            UserSessionRepository userSessionRepository,
            RefreshTokenRepository refreshTokenRepository,
            SecurityAuditEventRepository auditEventRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider,
            LoginAttemptService loginAttemptService,
            SecurityAuditService securityAuditService
    ) {
        this.userAccountRepository = userAccountRepository;
        this.userSessionRepository = userSessionRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.auditEventRepository = auditEventRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.loginAttemptService = loginAttemptService;
        this.securityAuditService = securityAuditService;
    }

    @Transactional
    public UserProfileDto register(RegisterRequestDto request, String ipAddress, String userAgent) {
        String normalizedEmail = request.getEmail().toLowerCase().trim();
        if (userAccountRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            securityAuditService.logEvent(AuditEventType.LOGIN_FAILURE, null, normalizedEmail, ipAddress, userAgent, AuditStatus.FAILURE, "Registration failed: Email already exists");
            throw new UserAlreadyExistsException("User with this email already exists");
        }

        String passwordHash = passwordEncoder.encode(request.getPassword());
        UserAccount user = UserAccount.createCustomer(normalizedEmail, passwordHash, request.getFirstName(), request.getLastName());
        UserAccount savedUser = userAccountRepository.save(user);

        securityAuditService.logEvent(AuditEventType.LOGIN_SUCCESS, savedUser.getId(), savedUser.getEmail(), ipAddress, userAgent, AuditStatus.SUCCESS, "User registered successfully");
        return new UserProfileDto(savedUser);
    }

    @Transactional
    public AuthTokenResponseDto login(LoginRequestDto request, String ipAddress, String userAgent) {
        String normalizedEmail = request.getEmail().toLowerCase().trim();
        Optional<UserAccount> userOpt = userAccountRepository.findByEmailIgnoreCase(normalizedEmail);

        if (userOpt.isEmpty()) {
            securityAuditService.logEvent(AuditEventType.LOGIN_FAILURE, null, normalizedEmail, ipAddress, userAgent, AuditStatus.FAILURE, "Login failed: Invalid credentials");
            throw new AuthenticationFailedException("Invalid credentials");
        }

        UserAccount user = userOpt.get();

        if (user.isAccountLocked()) {
            securityAuditService.logEvent(AuditEventType.ACCOUNT_LOCKED, user.getId(), user.getEmail(), ipAddress, userAgent, AuditStatus.FAILURE, "Login blocked: Account is locked");
            throw new AccountLockedException("Account is temporarily locked due to multiple failed login attempts. Please try again later.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            loginAttemptService.recordFailedAttempt(user);
            if (user.isAccountLocked()) {
                securityAuditService.logEvent(AuditEventType.ACCOUNT_LOCKED, user.getId(), user.getEmail(), ipAddress, userAgent, AuditStatus.SUSPICIOUS, "Account locked after failed attempt");
            } else {
                securityAuditService.logEvent(AuditEventType.LOGIN_FAILURE, user.getId(), user.getEmail(), ipAddress, userAgent, AuditStatus.FAILURE, "Login failed: Invalid credentials");
            }
            throw new AuthenticationFailedException("Invalid credentials");
        }

        // Reset failed login attempts on successful password verification
        loginAttemptService.recordSuccess(user);

        // Create Session
        Instant sessionExpiresAt = Instant.now().plusSeconds(refreshTokenExpirationDays * 86400);
        UserSession session = new UserSession(user.getId(), request.getDeviceInfo(), ipAddress, sessionExpiresAt);
        userSessionRepository.save(session);

        // Create Refresh Token
        String rawRefreshToken = generateSecureToken();
        String tokenHash = hashToken(rawRefreshToken);
        String tokenFamily = UUID.randomUUID().toString();

        RefreshToken refreshToken = new RefreshToken(session.getId(), user.getId(), tokenHash, tokenFamily, sessionExpiresAt);
        refreshTokenRepository.save(refreshToken);

        // Generate Access Token
        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getEmail(), user.getRole(), session.getId());

        securityAuditService.logEvent(AuditEventType.LOGIN_SUCCESS, user.getId(), user.getEmail(), ipAddress, userAgent, AuditStatus.SUCCESS, "Login successful");

        return new AuthTokenResponseDto(accessToken, rawRefreshToken, accessTokenExpirationMs, user.getId(), user.getEmail(), user.getRole().name(), session.getId());
    }

    @Transactional
    public AuthTokenResponseDto refreshToken(RefreshTokenRequestDto request, String ipAddress, String userAgent) {
        String rawRefreshToken = request.getRefreshToken();
        String tokenHash = hashToken(rawRefreshToken);

        Optional<RefreshToken> tokenOpt = refreshTokenRepository.findByTokenHash(tokenHash);

        if (tokenOpt.isEmpty()) {
            securityAuditService.logEvent(AuditEventType.UNAUTHORIZED_ACCESS, null, null, ipAddress, userAgent, AuditStatus.SUSPICIOUS, "Refresh failed: Unknown token hash");
            throw new InvalidTokenException("Invalid refresh token");
        }

        RefreshToken token = tokenOpt.get();

        // Token Reuse / Theft Detection Check
        if (token.isRotated() || token.isRevoked()) {
            log.warn("SECURITY ALERT: Refresh token reuse detected for family {} (User: {})", token.getTokenFamily(), token.getUserId());
            // Revoke entire token family and session
            refreshTokenRepository.revokeTokenFamily(token.getTokenFamily());
            userSessionRepository.findById(token.getSessionId()).ifPresent(UserSession::revoke);

            securityAuditService.logEvent(AuditEventType.TOKEN_REUSE_DETECTED, token.getUserId(), token.getTokenFamily(), ipAddress, userAgent, AuditStatus.SUSPICIOUS, "Token reuse detected! Revoked token family");
            throw new InvalidTokenException("Suspicious token activity detected. Session terminated.");
        }

        if (Instant.now().isAfter(token.getExpiresAt())) {
            securityAuditService.logEvent(AuditEventType.LOGIN_FAILURE, token.getUserId(), null, ipAddress, userAgent, AuditStatus.FAILURE, "Refresh failed: Token expired");
            throw new InvalidTokenException("Refresh token has expired");
        }

        Optional<UserAccount> userOpt = userAccountRepository.findById(token.getUserId());
        if (userOpt.isEmpty() || userOpt.get().isAccountLocked()) {
            throw new InvalidTokenException("Associated user account is unavailable or locked");
        }

        UserAccount user = userOpt.get();

        // Rotate Refresh Token
        token.markRotated();
        refreshTokenRepository.save(token);

        // Create new Refresh Token in same family
        String newRawRefreshToken = generateSecureToken();
        String newTokenHash = hashToken(newRawRefreshToken);
        Instant expiresAt = token.getExpiresAt();

        RefreshToken newRefreshToken = new RefreshToken(token.getSessionId(), user.getId(), newTokenHash, token.getTokenFamily(), expiresAt);
        refreshTokenRepository.save(newRefreshToken);

        // Update session last-used
        userSessionRepository.findById(token.getSessionId()).ifPresent(UserSession::touch);

        // Issue new Access Token
        String newAccessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getEmail(), user.getRole(), token.getSessionId());

        securityAuditService.logEvent(AuditEventType.TOKEN_ROTATION, user.getId(), token.getSessionId(), ipAddress, userAgent, AuditStatus.SUCCESS, "Token rotated successfully");

        return new AuthTokenResponseDto(newAccessToken, newRawRefreshToken, accessTokenExpirationMs, user.getId(), user.getEmail(), user.getRole().name(), token.getSessionId());
    }

    @Transactional
    public void logout(String userId, String sessionId, String ipAddress, String userAgent) {
        if (sessionId != null) {
            userSessionRepository.findById(sessionId).ifPresent(session -> {
                session.revoke();
                userSessionRepository.save(session);
            });
        }
        if (userId != null) {
            refreshTokenRepository.revokeAllUserTokens(userId);
        }
        securityAuditService.logEvent(AuditEventType.LOGOUT, userId, sessionId, ipAddress, userAgent, AuditStatus.SUCCESS, "User logged out");
    }

    @Transactional
    public void logoutAllSessions(String userId, String ipAddress, String userAgent) {
        userSessionRepository.revokeAllUserSessions(userId, Instant.now());
        refreshTokenRepository.revokeAllUserTokens(userId);
        securityAuditService.logEvent(AuditEventType.LOGOUT_ALL, userId, null, ipAddress, userAgent, AuditStatus.SUCCESS, "All user sessions revoked");
    }

    @Transactional
    public void changePassword(String userId, ChangePasswordRequestDto request, String ipAddress, String userAgent) {
        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new AuthenticationFailedException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            securityAuditService.logEvent(AuditEventType.PASSWORD_CHANGED, userId, null, ipAddress, userAgent, AuditStatus.FAILURE, "Password change failed: Incorrect current password");
            throw new AuthenticationFailedException("Current password is incorrect");
        }

        user.updatePassword(passwordEncoder.encode(request.getNewPassword()));
        userAccountRepository.save(user);

        // Invalidate all active sessions upon password change
        logoutAllSessions(userId, ipAddress, userAgent);

        securityAuditService.logEvent(AuditEventType.PASSWORD_CHANGED, userId, null, ipAddress, userAgent, AuditStatus.SUCCESS, "Password changed successfully");
    }

    public UserProfileDto getUserProfile(String userId) {
        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new AuthenticationFailedException("User not found"));
        return new UserProfileDto(user);
    }

    public List<UserSessionDto> getActiveSessions(String userId) {
        return userSessionRepository.findByUserIdAndRevokedAtIsNull(userId).stream()
                .filter(UserSession::isActive)
                .map(UserSessionDto::new)
                .toList();
    }

    @Transactional
    public void lockUserAccount(String userId, long lockDurationMinutes) {
        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new AuthenticationFailedException("User not found"));
        user.lockAccount(lockDurationMinutes);
        userAccountRepository.save(user);
        logoutAllSessions(userId, "ADMIN_ACTION", "ADMIN_ACTION");
        securityAuditService.logEvent(AuditEventType.ACCOUNT_LOCKED, "ADMIN", userId, "ADMIN", "ADMIN", AuditStatus.SUCCESS, "Account locked by admin");
    }

    @Transactional
    public void unlockUserAccount(String userId) {
        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new AuthenticationFailedException("User not found"));
        user.unlockAccount();
        userAccountRepository.save(user);
        securityAuditService.logEvent(AuditEventType.ACCOUNT_UNLOCKED, "ADMIN", userId, "ADMIN", "ADMIN", AuditStatus.SUCCESS, "Account unlocked by admin");
    }

    public Page<SecurityAuditEventDto> getAuditEvents(Pageable pageable) {
        return auditEventRepository.findAllByOrderByCreatedAtDesc(pageable).map(SecurityAuditEventDto::new);
    }

    private String generateSecureToken() {
        return UUID.randomUUID().toString() + "-" + UUID.randomUUID().toString();
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm unavailable", e);
        }
    }
}
