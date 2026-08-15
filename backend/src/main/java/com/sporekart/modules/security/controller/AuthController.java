package com.sporekart.modules.security.controller;

import com.sporekart.modules.security.application.AuthenticationApplicationService;
import com.sporekart.modules.security.application.dto.*;
import com.sporekart.modules.security.infrastructure.jwt.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Authentication & Identity", description = "JWT-based stateless authentication — registration, login, token refresh, session management, and account operations")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationApplicationService authService;

    public AuthController(AuthenticationApplicationService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @Operation(
            summary = "Register New Customer Account",
            description = "Creates a new customer account with email/password credentials. Returns the user profile on success.",
            security = {} // Public endpoint — no JWT required
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Account registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload (validation failure)"),
            @ApiResponse(responseCode = "409", description = "Email address already registered")
    })
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
    @Operation(
            summary = "Login — Obtain JWT Token Pair",
            description = "Authenticates with email and password. Returns a short-lived access token and a long-lived refresh token.",
            security = {} // Public endpoint — no JWT required
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful — JWT access and refresh tokens returned"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials"),
            @ApiResponse(responseCode = "423", description = "Account is locked due to repeated failed login attempts")
    })
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
    @Operation(
            summary = "Refresh Access Token",
            description = "Exchanges a valid refresh token for a new JWT access token pair. The old refresh token is rotated (invalidated).",
            security = {} // Uses refresh token in body, not Bearer header
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token refreshed successfully"),
            @ApiResponse(responseCode = "400", description = "Missing or malformed refresh token"),
            @ApiResponse(responseCode = "401", description = "Refresh token is invalid, expired, or already used")
    })
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
    @Operation(
            summary = "Logout Current Session",
            description = "Invalidates the current JWT session. The access token's associated refresh token is revoked.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Session logged out successfully"),
            @ApiResponse(responseCode = "401", description = "No valid authentication token provided")
    })
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
    @Operation(
            summary = "Logout All Active Sessions",
            description = "Revokes all active refresh token sessions for the authenticated user, forcing re-login on all devices.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "All sessions revoked successfully"),
            @ApiResponse(responseCode = "401", description = "No valid authentication token provided")
    })
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
    @Operation(
            summary = "Change Account Password",
            description = "Changes the password for the authenticated user account. All existing sessions are revoked on success.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Password changed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid current password or password validation failure"),
            @ApiResponse(responseCode = "401", description = "No valid authentication token provided")
    })
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
    @Operation(
            summary = "Get Current User Profile",
            description = "Returns the profile of the currently authenticated user.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User profile returned"),
            @ApiResponse(responseCode = "401", description = "No valid authentication token provided")
    })
    public ResponseEntity<UserProfileDto> getCurrentUser(Authentication authentication) {
        String userId = resolveUserId(authentication);
        log.info("REST: Request for current user profile: {}", userId);
        UserProfileDto result = authService.getUserProfile(userId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/sessions")
    @Operation(
            summary = "List Active Sessions",
            description = "Returns all active authenticated sessions for the current user, showing device and IP information.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Active sessions list returned"),
            @ApiResponse(responseCode = "401", description = "No valid authentication token provided")
    })
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
