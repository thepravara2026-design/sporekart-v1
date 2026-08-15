package com.sporekart.modules.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sporekart.modules.security.application.dto.*;
import com.sporekart.modules.security.domain.UserRole;
import com.sporekart.modules.security.infrastructure.jwt.JwtTokenProvider;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthorizationSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("Should reject unauthenticated access to protected customer endpoint with 401")
    void shouldRejectUnauthenticatedAccess() throws Exception {
        mockMvc.perform(get("/api/v1/orders"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
    }

    @Test
    @DisplayName("Should reject customer token attempting to access admin API with 403")
    void shouldRejectCustomerAccessToAdminApi() throws Exception {
        RegisterRequestDto reg = new RegisterRequestDto("custauth@sporekart.com", "Password123!", "Cust", "Auth");
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reg)))
                .andExpect(status().isCreated());

        LoginRequestDto login = new LoginRequestDto("custauth@sporekart.com", "Password123!");
        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn();

        AuthTokenResponseDto auth = objectMapper.readValue(loginResult.getResponse().getContentAsString(), AuthTokenResponseDto.class);

        mockMvc.perform(get("/api/v1/admin/security/audit-events")
                        .header("Authorization", "Bearer " + auth.getAccessToken()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("FORBIDDEN"));
    }

    @Test
    @DisplayName("Should allow valid admin token to access admin security APIs")
    void shouldAllowAdminAccessToAdminApi() throws Exception {
        String adminToken = jwtTokenProvider.generateAccessToken("admin-1", "admin1@sporekart.com", UserRole.ROLE_ADMIN, "admin-session-1");

        mockMvc.perform(get("/api/v1/admin/security/audit-events")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should reject tampered JWT token with 401")
    void shouldRejectTamperedJwtToken() throws Exception {
        String validToken = jwtTokenProvider.generateAccessToken("cust-101", "cust101@sporekart.com", UserRole.ROLE_CUSTOMER, "session-1");
        String tamperedToken = validToken.substring(0, validToken.length() - 5) + "X1Y2Z";

        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer " + tamperedToken))
                .andExpect(status().isUnauthorized());
    }
}
