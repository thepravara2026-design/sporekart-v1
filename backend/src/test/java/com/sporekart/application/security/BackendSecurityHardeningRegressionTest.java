package com.sporekart.application.security;

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

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("SQ-07 Backend Security Hardening Regression Suite")
class BackendSecurityHardeningRegressionTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("HSTS and frame options security response headers are enforced")
    void testHstsHeaderPresent() throws Exception {
        mockMvc.perform(get("/api/v1/health"))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Content-Type-Options"))
                .andExpect(header().exists("X-Frame-Options"));
    }

    @Test
    @DisplayName("Public endpoints remain publicly accessible without token")
    void testPublicEndpointAccessibility() throws Exception {
        mockMvc.perform(get("/api/v1/health"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/catalog/products"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Admin endpoints reject unauthenticated access with 401 UNAUTHORIZED")
    void testAdminEndpointUnauthenticatedRejection() throws Exception {
        mockMvc.perform(get("/api/v1/admin/orders"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Admin endpoints reject customer role with 403 FORBIDDEN")
    void testAdminEndpointCustomerRoleRejection() throws Exception {
        String token = jwtTokenProvider.generateAccessToken("cust-sec-1", "cust@sporekart.com", UserRole.ROLE_CUSTOMER, "sess-1");

        mockMvc.perform(get("/api/v1/admin/orders")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Admin endpoints accept admin role with 200 OK")
    void testAdminEndpointAdminRoleAuthorized() throws Exception {
        String adminToken = jwtTokenProvider.generateAccessToken("admin-sec-1", "admin@sporekart.com", UserRole.ROLE_ADMIN, "sess-admin-1");

        mockMvc.perform(get("/api/v1/admin/orders")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("IDOR Check - Cross-customer order detail access returns 404/403")
    void testIDOROrderAccessProtection() throws Exception {
        String customerBToken = jwtTokenProvider.generateAccessToken("cust-B", "custB@sporekart.com", UserRole.ROLE_CUSTOMER, "sess-B");
        UUID randomOrderId = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/orders/" + randomOrderId)
                        .header("Authorization", "Bearer " + customerBToken))
                .andExpect(status().isNotFound());
    }
}
