package com.sporekart.modules.catalog.domain;

import com.sporekart.modules.cart.domain.CartItem;
import com.sporekart.modules.cart.domain.exception.InvalidQuantityException;
import com.sporekart.modules.catalog.domain.category.Category;
import com.sporekart.modules.catalog.domain.product.Product;
import com.sporekart.modules.inventory.domain.InventoryItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Commerce Domain Boundary & Edge Case Hardening Protection")
class CommerceDomainBoundaryTest {

    @Test
    @DisplayName("Product SKU normalization handles valid SKUs and strips invalid characters")
    void testProductNormalizeSkuValid() {
        assertEquals("SPK-ITEM-100", Product.normalizeSku("  spk-item#100  "));
    }

    @Test
    @DisplayName("Product SKU normalization throws exception on blank or special-character-only SKUs")
    void testProductNormalizeSkuInvalid() {
        assertThrows(IllegalArgumentException.class, () -> Product.normalizeSku("   "));
        assertThrows(IllegalArgumentException.class, () -> Product.normalizeSku("!@#$%^&*()"));
    }

    @Test
    @DisplayName("Category slug generation handles valid names and converts to lowercase hyphenated slugs")
    void testCategoryGenerateSlugValid() {
        assertEquals("organic-mushrooms", Category.generateSlug("  Organic & Mushrooms!  "));
    }

    @Test
    @DisplayName("Category slug generation throws exception on special-character-only names")
    void testCategoryGenerateSlugInvalid() {
        assertThrows(IllegalArgumentException.class, () -> Category.generateSlug("   "));
        assertThrows(IllegalArgumentException.class, () -> Category.generateSlug("!@#$%^"));
    }

    @Test
    @DisplayName("CartItem quantity increment detects integer overflow safely")
    void testCartItemQuantityOverflowProtection() {
        CartItem item = CartItem.create(
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                "SKU-TEST-1",
                "Test Product",
                null,
                BigDecimal.TEN,
                5,
                1000
        );

        assertThrows(InvalidQuantityException.class, () -> item.incrementQuantity(Integer.MAX_VALUE - 2, 1000));
    }

    @Test
    @DisplayName("InventoryItem correctly validates available stock and bounds checks")
    void testInventoryItemAvailableQuantity() {
        InventoryItem item = InventoryItem.createNew(UUID.randomUUID(), null, "SKU-INV-1", 50);
        assertEquals(50, item.getAvailableQuantity());
        assertFalse(item.isLowStock());

        item.reserve(45);
        assertEquals(5, item.getAvailableQuantity());
        assertTrue(item.isLowStock());

        item.release(45);
        assertEquals(50, item.getAvailableQuantity());
    }
}
