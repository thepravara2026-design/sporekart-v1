package com.sporekart.modules.checkout.application;

import com.sporekart.modules.cart.domain.Cart;
import com.sporekart.modules.cart.domain.CartStatus;
import com.sporekart.modules.checkout.domain.exception.CartEmptyException;
import com.sporekart.modules.checkout.domain.exception.CheckoutNotEligibleException;
import com.sporekart.modules.checkout.domain.model.CheckoutPreview;

import com.sporekart.modules.checkout.domain.model.CheckoutWarning;
import com.sporekart.modules.checkout.domain.port.AvailabilityPort;
import com.sporekart.modules.checkout.domain.port.DiscountCalculatorPort;
import com.sporekart.modules.checkout.domain.port.ShippingRateProviderPort;
import com.sporekart.modules.checkout.domain.port.TaxCalculatorPort;
import com.sporekart.modules.checkout.infrastructure.adapter.ConfigurableTaxAdapter;
import com.sporekart.modules.checkout.infrastructure.adapter.FlatRateShippingAdapter;
import com.sporekart.modules.checkout.infrastructure.adapter.NoOpDiscountAdapter;
import com.sporekart.modules.checkout.infrastructure.config.CheckoutProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class CheckoutPricingServiceTest {

    private AvailabilityPort availabilityPort;
    private TaxCalculatorPort taxCalculatorPort;
    private ShippingRateProviderPort shippingRateProviderPort;
    private DiscountCalculatorPort discountCalculatorPort;
    private CheckoutPricingService pricingService;

    @BeforeEach
    void setUp() {
        availabilityPort = Mockito.mock(AvailabilityPort.class);

        CheckoutProperties properties = new CheckoutProperties();
        properties.setDefaultTaxRate(new BigDecimal("0.18"));
        properties.setFlatShippingFee(new BigDecimal("50.00"));
        properties.setFreeShippingThreshold(new BigDecimal("1000.00"));

        taxCalculatorPort = new ConfigurableTaxAdapter(properties);
        shippingRateProviderPort = new FlatRateShippingAdapter(properties);
        discountCalculatorPort = new NoOpDiscountAdapter();

        pricingService = new CheckoutPricingService(
                availabilityPort,
                taxCalculatorPort,
                shippingRateProviderPort,
                discountCalculatorPort
        );
    }

    @Test
    @DisplayName("Should throw CartEmptyException when checkout preview is requested for empty cart")
    void shouldThrowCartEmptyExceptionForEmptyCart() {
        Cart cart = Cart.createNewActiveCart("cust-1", "INR");
        assertThrows(CartEmptyException.class, () -> pricingService.calculateCheckoutPreview(cart, null, null));
    }

    @Test
    @DisplayName("Should throw CheckoutNotEligibleException when cart is not active")
    void shouldThrowCheckoutNotEligibleForNonActiveCart() {
        Cart cart = Cart.createNewActiveCart("cust-1", "INR");
        cart.markAsCheckedOut();
        assertThrows(CheckoutNotEligibleException.class, () -> pricingService.calculateCheckoutPreview(cart, null, null));
    }

    @Test
    @DisplayName("Should calculate authoritative preview and detect price change when Catalog price differs from Cart snapshot")
    void shouldCalculatePreviewAndDetectPriceChange() {
        UUID productId = UUID.randomUUID();
        Cart cart = Cart.createNewActiveCart("cust-1", "INR");

        // Cart snapshot price = 500.00
        cart.addItem(
                productId, null, "SKU-001", "Spore Mushroom Pack", null,
                new BigDecimal("500.00"), 2, 50, 100
        );

        // Catalog live price = 550.00
        when(availabilityPort.getAuthoritativeItemDetails(productId))
                .thenReturn(new AvailabilityPort.CatalogItemDetails(
                        productId, "Spore Mushroom Pack", "SKU-001",
                        new BigDecimal("550.00"), "INR", true, "ACTIVE"
                ));

        CheckoutPreview preview = pricingService.calculateCheckoutPreview(cart, "Mumbai", null);

        assertNotNull(preview);
        assertEquals(1, preview.getItems().size());
        assertEquals(1, preview.getWarnings().size());

        CheckoutWarning warning = preview.getWarnings().get(0);
        assertEquals(CheckoutWarning.WarningType.PRICE_CHANGED, warning.getType());

        // Subtotal = 550.00 * 2 = 1100.00
        assertEquals(new BigDecimal("1100.00"), preview.getSubtotal().getAmount());

        // Tax = 1100.00 * 0.18 = 198.00
        assertEquals(new BigDecimal("198.00"), preview.getTaxTotal().getAmount());

        // Subtotal (1100.00) >= Threshold (1000.00) -> Shipping fee = 0.00
        assertEquals(new BigDecimal("0.00"), preview.getShippingFee().getAmount());

        // Grand Total = 1100.00 - 0.00 + 198.00 + 0.00 = 1298.00
        assertEquals(new BigDecimal("1298.00"), preview.getGrandTotal().getAmount());
    }

    @Test
    @DisplayName("Should apply flat shipping fee when subtotal is below free shipping threshold")
    void shouldApplyFlatShippingFeeWhenBelowThreshold() {
        UUID productId = UUID.randomUUID();
        Cart cart = Cart.createNewActiveCart("cust-2", "INR");

        // Cart price = 400.00, Qty = 1
        cart.addItem(
                productId, null, "SKU-002", "Small Kit", null,
                new BigDecimal("400.00"), 1, 50, 100
        );

        when(availabilityPort.getAuthoritativeItemDetails(productId))
                .thenReturn(new AvailabilityPort.CatalogItemDetails(
                        productId, "Small Kit", "SKU-002",
                        new BigDecimal("400.00"), "INR", true, "ACTIVE"
                ));

        CheckoutPreview preview = pricingService.calculateCheckoutPreview(cart, null, null);

        // Subtotal = 400.00
        assertEquals(new BigDecimal("400.00"), preview.getSubtotal().getAmount());

        // Tax = 400.00 * 0.18 = 72.00
        assertEquals(new BigDecimal("72.00"), preview.getTaxTotal().getAmount());

        // Subtotal (400.00) < Threshold (1000.00) -> Shipping fee = 50.00
        assertEquals(new BigDecimal("50.00"), preview.getShippingFee().getAmount());

        // Grand Total = 400.00 + 72.00 + 50.00 = 522.00
        assertEquals(new BigDecimal("522.00"), preview.getGrandTotal().getAmount());
    }
}
