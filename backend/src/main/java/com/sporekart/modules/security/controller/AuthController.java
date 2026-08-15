package com.sporekart.modules.security.controller;

import com.sporekart.modules.security.application.AuthenticationApplicationService;
import com.sporekart.modules.security.application.dto.*;
import com.sporekart.modules.security.infrastructure.jwt.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationApplicationService authService;

    public AuthController(AuthenticationApplicationService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserProfileDto> register(
            @Valid @RequestBody RegisterRequestDto requestDto,
            HttpServletRequest httpRequest
    ) {
        log.info("REST: Request to register user with email: {}", requestDto.getEmail());
        String ipAddress = extractIpAddress(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        UserProfileDto result = authService.register(requestDto, ipAddress, userAgent);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthTokenResponseDto> login(
            @Valid @RequestBody LoginRequestDto requestDto,
            HttpServletRequest httpRequest
    ) {
        log.info("REST: Request to login user with email: {}", requestDto.getEmail());
        String ipAddress = extractIpAddress(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        AuthTokenResponseDto result = authService.login(requestDto, ipAddress, userAgent);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthTokenResponseDto> refreshToken(
            @Valid @RequestBody RefreshTokenRequestDto requestDto,
            HttpServletRequest httpRequest
    ) {
        log.info("REST: Request to refresh auth token");
        String ipAddress = extractIpAddress(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        AuthTokenResponseDto result = authService.refreshToken(requestDto, ipAddress, userAgent);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            Authentication authentication,
            HttpServletRequest httpRequest
    ) {
        String userId = resolveUserId(authentication);
        String sessionId = resolveSessionId(authentication);
        String ipAddress = extractIpAddress(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        log.info("REST: Request to logout user: {}", userId);
        authService.logout(userId, sessionId, ipAddress, userAgent);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/logout-all")
    public ResponseEntity<Void> logoutAll(
            Authentication authentication,
            HttpServletRequest httpRequest
    ) {
        String userId = resolveUserId(authentication);
        String ipAddress = extractIpAddress(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        log.info("REST: Request to logout all sessions for user: {}", userId);
        authService.logoutAllSessions(userId, ipAddress, userAgent);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(
            @Valid @RequestBody ChangePasswordRequestDto requestDto,
            Authentication authentication,
            HttpServletRequest httpRequest
    ) {
        String userId = resolveUserId(authentication);
        String ipAddress = extractIpAddress(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        log.info("REST: Request to change password for user: {}", userId);
        authService.changePassword(userId, requestDto, ipAddress, userAgent);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileDto> getCurrentUser(Authentication authentication) {
        String userId = resolveUserId(authentication);
        log.info("REST: Request for current user profile: {}", userId);
        UserProfileDto result = authService.getUserProfile(userId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/sessions")
    public ResponseEntity<List<UserSessionDto>> getActiveSessions(Authentication authentication) {
        String userId = resolveUserId(authentication);
        log.info("REST: Request for active sessions for user: {}", userId);
        List<UserSessionDto> sessions = authService.getActiveSessions(userId);
        return ResponseEntity.ok(sessions);
    }

    private String resolveUserId(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new org.springframework.security.authentication.AuthenticationCredentialsNotFoundException("Authentication required");
        }
        if (authentication.getPrincipal() instanceof UserPrincipal principal) {
            return principal.getId();
        }
        return authentication.getName();
    }

    private String resolveSessionId(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal principal) {
            return principal.getSessionId();
        }
        return null;
    }

    private String extractIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
