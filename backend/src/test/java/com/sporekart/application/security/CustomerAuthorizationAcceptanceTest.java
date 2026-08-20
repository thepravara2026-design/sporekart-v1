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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class CustomerAuthorizationAcceptanceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    // ──────────────────────────────────────────────────────────────────────
    // Helper: real JWT tokens
    // ──────────────────────────────────────────────────────────────────────

    private String customerToken() {
        return jwtTokenProvider.generateAccessToken(
                "cust-101", "customer@sporekart.com", UserRole.ROLE_CUSTOMER, "session-cust"
        );
    }

    private String adminToken() {
        return jwtTokenProvider.generateAccessToken(
                "admin-001", "admin@sporekart.com", UserRole.ROLE_ADMIN, "session-admin"
        );
    }

    // ──────────────────────────────────────────────────────────────────────
    // 1. Unauthenticated access (no token) → 401 Unauthorized
    // ──────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("AUTH-001: GET /api/v1/cart without token should return 401 Unauthorized")
    void testGetCartWithoutTokenReturns401() throws Exception {
        mockMvc.perform(get("/api/v1/cart")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("AUTH-002: POST /api/v1/cart/items without token should return 401 Unauthorized")
    void testAddCartItemWithoutTokenReturns401() throws Exception {
        mockMvc.perform(post("/api/v1/cart/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":\"" + UUID.randomUUID() + "\",\"quantity\":1}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("AUTH-003: PATCH /api/v1/cart/items/{uuid} without token should return 401 Unauthorized")
    void testUpdateCartItemWithoutTokenReturns401() throws Exception {
        UUID itemId = UUID.randomUUID();

        mockMvc.perform(patch("/api/v1/cart/items/" + itemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\":2}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("AUTH-004: DELETE /api/v1/cart/items/{uuid} without token should return 401 Unauthorized")
    void testRemoveCartItemWithoutTokenReturns401() throws Exception {
        UUID itemId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/cart/items/" + itemId))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("AUTH-005: DELETE /api/v1/cart/items (clear cart) without token should return 401 Unauthorized")
    void testClearCartWithoutTokenReturns401() throws Exception {
        mockMvc.perform(delete("/api/v1/cart/items"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("AUTH-006: POST /api/v1/checkout/preview without token should return 401 Unauthorized")
    void testCheckoutWithoutTokenReturns401() throws Exception {
        mockMvc.perform(post("/api/v1/checkout/preview")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("AUTH-007: POST /api/v1/payments without token should return 401 Unauthorized")
    void testPaymentInitiationWithoutTokenReturns401() throws Exception {
        mockMvc.perform(post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"orderId\":\"" + UUID.randomUUID() + "\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("AUTH-008: GET /api/v1/orders without token should return 401 Unauthorized")
    void testGetOrdersWithoutTokenReturns401() throws Exception {
        mockMvc.perform(get("/api/v1/orders"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("AUTH-009: GET /api/v1/orders/{uuid} without token should return 401 Unauthorized")
    void testGetOrderDetailWithoutTokenReturns401() throws Exception {
        UUID orderId = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/orders/" + orderId))
                .andExpect(status().isUnauthorized());
    }

    // ──────────────────────────────────────────────────────────────────────
    // 2. Wrong role access → 403 Forbidden
    // ──────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("AUTH-010: Admin token accessing /api/v1/admin/orders (no customer role) should return 200")
    void testAdminAccessingAdminOrdersReturns200() throws Exception {
        mockMvc.perform(get("/api/v1/admin/orders")
                        .header("Authorization", "Bearer " + adminToken())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("AUTH-011: Customer token accessing admin orders endpoint should return 403 Forbidden")
    void testCustomerAccessingAdminOrdersReturns403() throws Exception {
        mockMvc.perform(get("/api/v1/admin/orders")
                        .header("Authorization", "Bearer " + customerToken())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    // ──────────────────────────────────────────────────────────────────────
    // 3. Valid customer access → 200 OK (or non-401/403)
    // ──────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("AUTH-012: Valid customer token accessing GET /api/v1/cart should return 200 OK")
    void testValidCustomerGetCartReturns200() throws Exception {
        mockMvc.perform(get("/api/v1/cart")
                        .header("Authorization", "Bearer " + customerToken())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("AUTH-013: Valid customer token accessing GET /api/v1/orders should return 200 OK")
    void testValidCustomerGetOrdersReturns200() throws Exception {
        mockMvc.perform(get("/api/v1/orders")
                        .header("Authorization", "Bearer " + customerToken())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    // ──────────────────────────────────────────────────────────────────────
    // 4. IDOR protection: Customer A accessing Customer B's order → 404
    // ──────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("AUTH-014: IDOR — Customer B accessing Customer A's order should return 404 Not Found")
    void testIdorOrderAccessReturns404() throws Exception {
        String customerBToken = jwtTokenProvider.generateAccessToken(
                "cust-202", "customerB@sporekart.com", UserRole.ROLE_CUSTOMER, "session-cust-b"
        );
        UUID customerAOrderId = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/orders/" + customerAOrderId)
                        .header("Authorization", "Bearer " + customerBToken))
                .andExpect(status().isNotFound());
    }

    // ──────────────────────────────────────────────────────────────────────
    // 5. Catalog public access (no auth needed) → 200 OK
    // ──────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("AUTH-015: GET /api/v1/catalog/products without token should return 200 OK (public)")
    void testCatalogProductsPublicAccess() throws Exception {
        mockMvc.perform(get("/api/v1/catalog/products"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("AUTH-016: GET /api/v1/catalog/categories without token should return 200 OK (public)")
    void testCatalogCategoriesPublicAccess() throws Exception {
        mockMvc.perform(get("/api/v1/catalog/categories"))
                .andExpect(status().isOk());
    }
}
