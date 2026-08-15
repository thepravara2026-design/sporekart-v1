package com.sporekart.modules.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sporekart.modules.security.application.dto.*;
import com.sporekart.modules.security.infrastructure.persistence.UserAccountRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthenticationSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Test
    @DisplayName("Should successfully register new user with normalized email")
    void shouldRegisterNewUser() throws Exception {
        RegisterRequestDto request = new RegisterRequestDto("testuser1@sporekart.com", "Password123!", "Jane", "Doe");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("testuser1@sporekart.com"))
                .andExpect(jsonPath("$.role").value("ROLE_CUSTOMER"));

        assertThat(userAccountRepository.existsByEmailIgnoreCase("testuser1@sporekart.com")).isTrue();
    }

    @Test
    @DisplayName("Should reject registration if email already exists")
    void shouldRejectDuplicateRegistration() throws Exception {
        RegisterRequestDto request = new RegisterRequestDto("duplicate@sporekart.com", "Password123!", "John", "Doe");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("USER_ALREADY_EXISTS"));
    }

    @Test
    @DisplayName("Should login successfully and return access and refresh tokens")
    void shouldLoginSuccessfully() throws Exception {
        RegisterRequestDto reg = new RegisterRequestDto("loginuser@sporekart.com", "Password123!", "John", "Doe");
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reg)))
                .andExpect(status().isCreated());

        LoginRequestDto login = new LoginRequestDto("loginuser@sporekart.com", "Password123!", "Chrome Desktop");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.role").value("ROLE_CUSTOMER"));
    }

    @Test
    @DisplayName("Should trigger account lockout after 5 consecutive failed login attempts")
    void shouldLockAccountAfterFailedAttempts() throws Exception {
        RegisterRequestDto reg = new RegisterRequestDto("lockuser@sporekart.com", "Password123!", "Lock", "Me");
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reg)))
                .andExpect(status().isCreated());

        LoginRequestDto badLogin = new LoginRequestDto("lockuser@sporekart.com", "WrongPassword!");

        // 5 Failed Login Attempts
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(badLogin)))
                    .andExpect(status().isUnauthorized());
        }

        // 6th Attempt with CORRECT password should now be BLOCKED due to lockout
        LoginRequestDto goodLogin = new LoginRequestDto("lockuser@sporekart.com", "Password123!");
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(goodLogin)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("ACCOUNT_LOCKED"));
    }

    @Test
    @DisplayName("Should rotate refresh tokens and detect token reuse attacks")
    void shouldRotateRefreshTokenAndDetectReuse() throws Exception {
        RegisterRequestDto reg = new RegisterRequestDto("rotation@sporekart.com", "Password123!", "Rotate", "Me");
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reg)))
                .andExpect(status().isCreated());

        LoginRequestDto login = new LoginRequestDto("rotation@sporekart.com", "Password123!");
        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn();

        AuthTokenResponseDto auth1 = objectMapper.readValue(loginResult.getResponse().getContentAsString(), AuthTokenResponseDto.class);
        String initialRefreshToken = auth1.getRefreshToken();

        // 1st Refresh: Valid rotation
        RefreshTokenRequestDto refreshReq1 = new RefreshTokenRequestDto(initialRefreshToken);
        MvcResult refreshResult1 = mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshReq1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andReturn();

        AuthTokenResponseDto auth2 = objectMapper.readValue(refreshResult1.getResponse().getContentAsString(), AuthTokenResponseDto.class);
        String rotatedRefreshToken = auth2.getRefreshToken();
        assertThat(rotatedRefreshToken).isNotEqualTo(initialRefreshToken);

        // 2nd Refresh using initial (ROTATED/OLD) Refresh Token -> REUSE DETECTED!
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshReq1)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("INVALID_TOKEN"));

        // 3rd Refresh using rotatedRefreshToken should NOW FAIL because the entire token family was revoked!
        RefreshTokenRequestDto refreshReq2 = new RefreshTokenRequestDto(rotatedRefreshToken);
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshReq2)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("INVALID_TOKEN"));
    }
}
