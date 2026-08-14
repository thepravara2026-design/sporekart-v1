package com.sporekart.modules.checkout.application;

import com.sporekart.modules.cart.domain.Cart;
import com.sporekart.modules.cart.domain.CartStatus;
import com.sporekart.modules.cart.domain.exception.CartNotFoundException;
import com.sporekart.modules.cart.infrastructure.persistence.CartRepository;
import com.sporekart.modules.checkout.application.dto.CheckoutPreviewRequest;
import com.sporekart.modules.checkout.application.dto.CheckoutPreviewResponse;
import com.sporekart.modules.checkout.domain.model.CheckoutPreview;
import com.sporekart.modules.checkout.domain.model.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class CheckoutApplicationServiceTest {

    private CartRepository cartRepository;
    private CheckoutPricingService checkoutPricingService;
    private CheckoutApplicationService applicationService;

    @BeforeEach
    void setUp() {
        cartRepository = Mockito.mock(CartRepository.class);
        checkoutPricingService = Mockito.mock(CheckoutPricingService.class);
        applicationService = new CheckoutApplicationService(cartRepository, checkoutPricingService);
    }

    @Test
    @DisplayName("Should generate checkout preview response for valid customer cart")
    void shouldGenerateCheckoutPreviewResponse() {
        String customerId = "cust-app-1";
        Cart cart = Cart.createNewActiveCart(customerId, "INR");
        when(cartRepository.findByCustomerIdAndStatus(customerId, CartStatus.ACTIVE)).thenReturn(Optional.of(cart));

        CheckoutPreview domainPreview = new CheckoutPreview(
                UUID.randomUUID(),
                cart.getId(),
                customerId,
                "INR",
                Collections.emptyList(),
                Money.of(new BigDecimal("100.00"), "INR"),
                Money.zero("INR"),
                Money.of(new BigDecimal("18.00"), "INR"),
                Money.of(new BigDecimal("50.00"), "INR"),
                Money.of(new BigDecimal("168.00"), "INR"),
                Collections.emptyList(),
                OffsetDateTime.now()
        );

        when(checkoutPricingService.calculateCheckoutPreview(eq(cart), any(), any())).thenReturn(domainPreview);

        CheckoutPreviewRequest request = new CheckoutPreviewRequest("Delhi", null);
        CheckoutPreviewResponse response = applicationService.generateCheckoutPreview(customerId, request);

        assertNotNull(response);
        assertEquals(cart.getId(), response.cartId());
        assertEquals(customerId, response.customerId());
        assertEquals(new BigDecimal("168.00"), response.breakdown().grandTotal());
    }

    @Test
    @DisplayName("Should throw CartNotFoundException if active cart does not exist for customer")
    void shouldThrowCartNotFoundExceptionWhenNoActiveCart() {
        String customerId = "cust-nonexistent";
        when(cartRepository.findByCustomerIdAndStatus(customerId, CartStatus.ACTIVE)).thenReturn(Optional.empty());

        assertThrows(CartNotFoundException.class, () ->
                applicationService.generateCheckoutPreview(customerId, null)
        );
    }
}
