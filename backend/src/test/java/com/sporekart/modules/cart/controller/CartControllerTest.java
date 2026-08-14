package com.sporekart.modules.cart.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sporekart.modules.cart.application.CartApplicationService;
import com.sporekart.modules.cart.application.dto.AddCartItemCommand;
import com.sporekart.modules.cart.application.dto.CartDto;
import com.sporekart.modules.cart.application.dto.CartItemDto;
import com.sporekart.modules.cart.application.dto.UpdateCartItemCommand;
import com.sporekart.modules.catalog.domain.exception.ProductNotFoundException;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CartApplicationService cartApplicationService;

    private final String customerId = "customer-1";
    private final UUID cartId = UUID.randomUUID();
    private final UUID productId = UUID.randomUUID();
    private final UUID itemId = UUID.randomUUID();
    private CartDto mockCartDto;

    @BeforeEach
    void setUp() {
        CartItemDto itemDto = new CartItemDto(
                itemId, productId, null, "SHROOM-001", "Reishi Spore Culture", null,
                new BigDecimal("20.00"), 2, new BigDecimal("40.00"), OffsetDateTime.now(), OffsetDateTime.now()
        );
        mockCartDto = new CartDto(
                cartId, customerId, "ACTIVE", "USD", new BigDecimal("40.00"), 2,
                List.of(itemDto), OffsetDateTime.now(), OffsetDateTime.now()
        );
    }

    @Test
    @WithMockUser(username = customerId)
    @DisplayName("GET /api/v1/cart should return 200 OK with active cart")
    void shouldGetActiveCart() throws Exception {
        when(cartApplicationService.getOrCreateActiveCart(customerId)).thenReturn(mockCartDto);

        mockMvc.perform(get("/api/v1/cart")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(cartId.toString()))
                .andExpect(jsonPath("$.data.customerId").value(customerId))
                .andExpect(jsonPath("$.data.itemCount").value(2))
                .andExpect(jsonPath("$.data.subtotal").value(40.00))
                .andExpect(header().exists("X-Request-ID"));
    }

    @Test
    @DisplayName("GET /api/v1/cart should return 401 Unauthorized for unauthenticated requests")
    void shouldReturn401ForUnauthenticatedGetCart() throws Exception {
        mockMvc.perform(get("/api/v1/cart")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
    }

    @Test
    @WithMockUser(username = customerId)
    @DisplayName("POST /api/v1/cart/items should add item and return 201 Created")
    void shouldAddItemToCart() throws Exception {
        AddCartItemCommand command = new AddCartItemCommand(productId, null, 2);
        when(cartApplicationService.addItemToCart(eq(customerId), any(AddCartItemCommand.class))).thenReturn(mockCartDto);

        mockMvc.perform(post("/api/v1/cart/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(command))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(cartId.toString()))
                .andExpect(jsonPath("$.data.items[0].sku").value("SHROOM-001"));
    }

    @Test
    @WithMockUser(username = customerId)
    @DisplayName("POST /api/v1/cart/items should return 400 Bad Request for zero or negative quantity")
    void shouldReturn400ForInvalidQuantityOnAdd() throws Exception {
        AddCartItemCommand command = new AddCartItemCommand(productId, null, 0);

        mockMvc.perform(post("/api/v1/cart/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(command))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }

    @Test
    @WithMockUser(username = customerId)
    @DisplayName("POST /api/v1/cart/items should return 404 Not Found if product does not exist in catalog")
    void shouldReturn404WhenProductNotFound() throws Exception {
        AddCartItemCommand command = new AddCartItemCommand(productId, null, 1);
        when(cartApplicationService.addItemToCart(eq(customerId), any(AddCartItemCommand.class)))
                .thenThrow(new ProductNotFoundException(productId));

        mockMvc.perform(post("/api/v1/cart/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(command))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("CATALOG_PRODUCT_NOT_FOUND"));
    }

    @Test
    @WithMockUser(username = customerId)
    @DisplayName("PATCH /api/v1/cart/items/{itemId} should update quantity and return 200 OK")
    void shouldUpdateCartItemQuantity() throws Exception {
        UpdateCartItemCommand command = new UpdateCartItemCommand(4);
        when(cartApplicationService.updateCartItemQuantity(eq(customerId), eq(itemId), any(UpdateCartItemCommand.class))).thenReturn(mockCartDto);

        mockMvc.perform(patch("/api/v1/cart/items/" + itemId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(command))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(username = customerId)
    @DisplayName("DELETE /api/v1/cart/items/{itemId} should remove item and return 200 OK")
    void shouldRemoveCartItem() throws Exception {
        when(cartApplicationService.removeCartItem(customerId, itemId)).thenReturn(mockCartDto);

        mockMvc.perform(delete("/api/v1/cart/items/" + itemId)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(username = customerId)
    @DisplayName("DELETE /api/v1/cart/items should clear cart and return 200 OK")
    void shouldClearCart() throws Exception {
        CartDto emptyCartDto = new CartDto(
                cartId, customerId, "ACTIVE", "USD", BigDecimal.ZERO, 0,
                List.of(), OffsetDateTime.now(), OffsetDateTime.now()
        );
        when(cartApplicationService.clearCart(customerId)).thenReturn(emptyCartDto);

        mockMvc.perform(delete("/api/v1/cart/items")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.itemCount").value(0))
                .andExpect(jsonPath("$.data.subtotal").value(0));
    }
}
