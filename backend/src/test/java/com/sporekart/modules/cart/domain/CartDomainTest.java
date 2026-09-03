package com.sporekart.modules.cart.domain;

import com.sporekart.modules.cart.domain.exception.CartItemNotFoundException;
import com.sporekart.modules.cart.domain.exception.CartNotModifiableException;
import com.sporekart.modules.cart.domain.exception.InvalidQuantityException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CartDomainTest {

    private Cart cart;
    private final String customerId = "cust-100";
    private final UUID productId = UUID.randomUUID();
    private final UUID variantId = UUID.randomUUID();
    private final String sku = "SHROOM-001";
    private final String productName = "Golden Teacher Spore Syringe";
    private final BigDecimal unitPrice = new BigDecimal("25.00");

    @BeforeEach
    void setUp() {
        cart = Cart.createNewActiveCart(customerId, "INR");
    }

    @Test
    @DisplayName("Should create new active cart with zero subtotal and item count")
    void shouldCreateNewActiveCart() {
        assertNotNull(cart.getId());
        assertEquals(customerId, cart.getCustomerId());
        assertEquals(CartStatus.ACTIVE, cart.getStatus());
        assertEquals("INR", cart.getCurrency());
        assertEquals(BigDecimal.ZERO, cart.getSubtotal());
        assertEquals(0, cart.getItemCount());
        assertTrue(cart.getItems().isEmpty());
    }

    @Test
    @DisplayName("Should add new item to cart and calculate line total and subtotal")
    void shouldAddItemToCart() {
        CartItem item = cart.addItem(productId, variantId, sku, productName, "10ml", unitPrice, 2, 50, 100);

        assertNotNull(item);
        assertEquals(1, cart.getItems().size());
        assertEquals(2, cart.getItemCount());
        assertEquals(new BigDecimal("50.00"), cart.getSubtotal());
        assertEquals(new BigDecimal("50.00"), item.getLineTotal());
    }

    @Test
    @DisplayName("Should merge quantities when adding duplicate product variant")
    void shouldMergeDuplicateItems() {
        cart.addItem(productId, variantId, sku, productName, "10ml", unitPrice, 2, 50, 100);
        cart.addItem(productId, variantId, sku, productName, "10ml", unitPrice, 3, 50, 100);

        assertEquals(1, cart.getItems().size());
        assertEquals(5, cart.getItemCount());
        assertEquals(new BigDecimal("125.00"), cart.getSubtotal());
        assertEquals(5, cart.getItems().get(0).getQuantity());
    }

    @Test
    @DisplayName("Should update cart item quantity")
    void shouldUpdateCartItemQuantity() {
        CartItem item = cart.addItem(productId, variantId, sku, productName, "10ml", unitPrice, 2, 50, 100);
        cart.updateItemQuantity(item.getId(), 4, 50, 100);

        assertEquals(4, cart.getItemCount());
        assertEquals(new BigDecimal("100.00"), cart.getSubtotal());
        assertEquals(4, item.getQuantity());
    }

    @Test
    @DisplayName("Should remove item from cart")
    void shouldRemoveCartItem() {
        CartItem item = cart.addItem(productId, variantId, sku, productName, "10ml", unitPrice, 2, 50, 100);
        cart.removeItem(item.getId());

        assertTrue(cart.getItems().isEmpty());
        assertEquals(0, cart.getItemCount());
        assertEquals(BigDecimal.ZERO, cart.getSubtotal());
    }

    @Test
    @DisplayName("Should throw exception when removing non-existent item")
    void shouldThrowExceptionWhenRemovingNonExistentItem() {
        assertThrows(CartItemNotFoundException.class, () -> cart.removeItem(UUID.randomUUID()));
    }

    @Test
    @DisplayName("Should clear all items from cart")
    void shouldClearCart() {
        cart.addItem(productId, null, sku, productName, null, unitPrice, 2, 50, 100);
        cart.addItem(UUID.randomUUID(), null, "SKU-002", "Other Product", null, new BigDecimal("10.00"), 1, 50, 100);
        assertEquals(2, cart.getItems().size());

        cart.clear();

        assertTrue(cart.getItems().isEmpty());
        assertEquals(0, cart.getItemCount());
        assertEquals(BigDecimal.ZERO, cart.getSubtotal());
    }

    @Test
    @DisplayName("Should reject adding item with invalid quantity")
    void shouldRejectInvalidQuantity() {
        assertThrows(InvalidQuantityException.class, () ->
                cart.addItem(productId, variantId, sku, productName, null, unitPrice, 0, 50, 100));
        assertThrows(InvalidQuantityException.class, () ->
                cart.addItem(productId, variantId, sku, productName, null, unitPrice, -1, 50, 100));
    }

    @Test
    @DisplayName("Should reject adding item exceeding max item quantity limit")
    void shouldRejectExceedingMaxQuantity() {
        assertThrows(InvalidQuantityException.class, () ->
                cart.addItem(productId, variantId, sku, productName, null, unitPrice, 51, 50, 100));
    }

    @Test
    @DisplayName("Should reject modifications to non-active cart")
    void shouldRejectModificationsToNonActiveCart() {
        cart.addItem(productId, variantId, sku, productName, null, unitPrice, 1, 50, 100);
        cart.markAsCheckedOut();

        assertThrows(CartNotModifiableException.class, () ->
                cart.addItem(productId, null, "SKU-999", "New Item", null, unitPrice, 1, 50, 100));
        assertThrows(CartNotModifiableException.class, () -> cart.clear());
    }
}
