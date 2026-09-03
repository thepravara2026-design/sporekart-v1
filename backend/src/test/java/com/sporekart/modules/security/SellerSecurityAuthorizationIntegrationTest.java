package com.sporekart.modules.security;

import com.sporekart.modules.security.domain.UserRole;
import com.sporekart.modules.security.infrastructure.jwt.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class SellerSecurityAuthorizationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("SEC-1.3-001: GROWER authority is denied access (403) to seller endpoints")
    void testGrowerRoleDeniedOnSellerEndpoint() throws Exception {
        String growerToken = jwtTokenProvider.generateAccessToken("grower-user-1", "grower@sporekart.com", UserRole.ROLE_GROWER, "sess-1");

        mockMvc.perform(get("/api/v1/seller/dashboard")
                        .header("Authorization", "Bearer " + growerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("SEC-1.3-002: SELLER authority is granted access (200) to seller endpoints")
    void testSellerRoleAllowedOnSellerEndpoint() throws Exception {
        String sellerToken = jwtTokenProvider.generateAccessToken("seller-user-1", "seller@sporekart.com", UserRole.ROLE_SELLER, "sess-1");

        mockMvc.perform(get("/api/v1/seller/dashboard")
                        .header("Authorization", "Bearer " + sellerToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("SEC-1.3-003: SELLER authority is denied access (403) to grower endpoints")
    void testSellerRoleDeniedOnGrowerEndpoint() throws Exception {
        String sellerToken = jwtTokenProvider.generateAccessToken("seller-user-1", "seller@sporekart.com", UserRole.ROLE_SELLER, "sess-1");

        mockMvc.perform(get("/api/v1/grower/dashboard")
                        .header("Authorization", "Bearer " + sellerToken))
                .andExpect(status().isForbidden());
    }
}
