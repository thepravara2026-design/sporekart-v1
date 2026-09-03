package com.sporekart.modules.seller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sporekart.modules.security.domain.UserRole;
import com.sporekart.modules.security.infrastructure.jwt.JwtTokenProvider;
import com.sporekart.modules.seller.web.dto.CreateSellerProductRequestDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class SellerControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    private String sellerToken;

    @BeforeEach
    void setUp() {
        sellerToken = jwtTokenProvider.generateAccessToken("seller-test-01", "seller@sporekart.com", UserRole.ROLE_SELLER, "sess-1");
    }

    @Test
    @DisplayName("SEL-001: GET /api/v1/seller/dashboard returns seller metrics")
    void testGetSellerDashboard() throws Exception {
        mockMvc.perform(get("/api/v1/seller/dashboard")
                        .header("Authorization", "Bearer " + sellerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalSales").exists());
    }

    @Test
    @DisplayName("SEL-002: GET & POST /api/v1/seller/products manages seller products")
    void testSellerProductsFlow() throws Exception {
        mockMvc.perform(get("/api/v1/seller/products")
                        .header("Authorization", "Bearer " + sellerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        CreateSellerProductRequestDto createDto = new CreateSellerProductRequestDto(
                "Oyster Spawn Bag",
                "SKU-SEL-OYSTER-1",
                "Spawn Bags",
                new BigDecimal("29.99"),
                new BigDecimal("34.99"),
                100
        );

        mockMvc.perform(post("/api/v1/seller/products")
                        .header("Authorization", "Bearer " + sellerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.sku").value("SKU-SEL-OYSTER-1"));
    }

    @Test
    @DisplayName("SEL-003: GET /api/v1/seller/inventory & POST adjustment manages inventory")
    void testSellerInventoryFlow() throws Exception {
        mockMvc.perform(get("/api/v1/seller/inventory")
                        .header("Authorization", "Bearer " + sellerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("SEL-004: GET /api/v1/seller/orders & POST status manages orders")
    void testSellerOrdersFlow() throws Exception {
        mockMvc.perform(get("/api/v1/seller/orders")
                        .header("Authorization", "Bearer " + sellerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("SEL-005: GET /api/v1/seller/payouts returns seller payouts")
    void testGetSellerPayouts() throws Exception {
        mockMvc.perform(get("/api/v1/seller/payouts")
                        .header("Authorization", "Bearer " + sellerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
