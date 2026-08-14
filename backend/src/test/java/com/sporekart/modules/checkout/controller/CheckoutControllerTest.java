package com.sporekart.modules.checkout.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sporekart.modules.checkout.application.CheckoutApplicationService;
import com.sporekart.modules.checkout.application.dto.*;
import com.sporekart.modules.checkout.domain.exception.CartEmptyException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CheckoutControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CheckoutApplicationService checkoutApplicationService;

    private final String customerId = "customer-chk-1";
    private final UUID cartId = UUID.randomUUID();
    private final UUID previewId = UUID.randomUUID();
    private final UUID productId = UUID.randomUUID();
    private final UUID cartItemId = UUID.randomUUID();
    private CheckoutPreviewResponse mockResponse;

    @BeforeEach
    void setUp() {
        CheckoutLineResponse lineResponse = new CheckoutLineResponse(
                cartItemId, productId, "SKU-CHK-1", "Oyster Spore Kit", 2,
                new BigDecimal("500.00"), new BigDecimal("500.00"), false,
                new BigDecimal("1000.00"), BigDecimal.ZERO, new BigDecimal("180.00"), new BigDecimal("1180.00")
        );

        PriceBreakdownResponse breakdown = new PriceBreakdownResponse(
                new BigDecimal("1000.00"), BigDecimal.ZERO, new BigDecimal("180.00"),
                BigDecimal.ZERO, new BigDecimal("1180.00"), "INR"
        );

        mockResponse = new CheckoutPreviewResponse(
                previewId, cartId, customerId, "INR",
                List.of(lineResponse), breakdown, List.of(), OffsetDateTime.now()
        );
    }

    @Test
    @WithMockUser(username = customerId)
    @DisplayName("POST /api/v1/checkout/preview should return 200 OK with server-authoritative preview")
    void shouldReturnCheckoutPreviewForAuthenticatedUser() throws Exception {
        CheckoutPreviewRequest request = new CheckoutPreviewRequest("Mumbai", null);
        when(checkoutApplicationService.generateCheckoutPreview(eq(customerId), any(CheckoutPreviewRequest.class)))
                .thenReturn(mockResponse);

        mockMvc.perform(post("/api/v1/checkout/preview")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.previewId").value(previewId.toString()))
                .andExpect(jsonPath("$.data.cartId").value(cartId.toString()))
                .andExpect(jsonPath("$.data.customerId").value(customerId))
                .andExpect(jsonPath("$.data.breakdown.subtotal").value(1000.00))
                .andExpect(jsonPath("$.data.breakdown.taxTotal").value(180.00))
                .andExpect(jsonPath("$.data.breakdown.grandTotal").value(1180.00))
                .andExpect(header().exists("X-Request-ID"));
    }

    @Test
    @DisplayName("POST /api/v1/checkout/preview should return 401 Unauthorized for unauthenticated requests")
    void shouldReturn401ForUnauthenticatedRequest() throws Exception {
        mockMvc.perform(post("/api/v1/checkout/preview")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
    }

    @Test
    @WithMockUser(username = customerId)
    @DisplayName("POST /api/v1/checkout/preview should return 400 Bad Request if cart is empty")
    void shouldReturn400WhenCartIsEmpty() throws Exception {
        when(checkoutApplicationService.generateCheckoutPreview(eq(customerId), any()))
                .thenThrow(new CartEmptyException(cartId));

        mockMvc.perform(post("/api/v1/checkout/preview")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("CHECKOUT_CART_EMPTY"));
    }
}
