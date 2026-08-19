package com.sporekart.modules.catalog.domain;

import com.sporekart.modules.catalog.domain.product.Product;
import com.sporekart.modules.catalog.domain.product.ProductStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ProductDomainTest {

    @Test
    void shouldCreateProductWithValidArguments() {
        Product product = Product.create("sp-shi-001", "Shiitake Spores", "Fresh spores", new BigDecimal("19.99"), "INR", null);

        assertNotNull(product.getId());
        assertEquals("SP-SHI-001", product.getSku());
        assertEquals("Shiitake Spores", product.getName());
        assertEquals(new BigDecimal("19.99"), product.getPrice());
        assertNull(product.getStrikeOutPrice());
        assertEquals(ProductStatus.DRAFT, product.getStatus());
        assertNotNull(product.getCreatedAt());
    }

    @Test
    void shouldCreateProductWithValidStrikeOutPrice() {
        Product product = Product.create("SP-LION-001", "Lion's Mane", "Desc", new BigDecimal("1999.00"), new BigDecimal("2499.00"), "INR", null);

        assertEquals(new BigDecimal("1999.00"), product.getPrice());
        assertEquals(new BigDecimal("2499.00"), product.getStrikeOutPrice());
    }

    @Test
    void shouldRejectStrikeOutPriceEqualToSellingPrice() {
        assertThrows(IllegalArgumentException.class, () ->
                Product.create("SP-SHI-001", "Shiitake", "Desc", new BigDecimal("1999.00"), new BigDecimal("1999.00"), "INR", null));
    }

    @Test
    void shouldRejectStrikeOutPriceLessThanSellingPrice() {
        assertThrows(IllegalArgumentException.class, () ->
                Product.create("SP-SHI-001", "Shiitake", "Desc", new BigDecimal("1999.00"), new BigDecimal("1499.00"), "INR", null));
    }

    @Test
    void shouldRejectNegativeStrikeOutPrice() {
        assertThrows(IllegalArgumentException.class, () ->
                Product.create("SP-SHI-001", "Shiitake", "Desc", new BigDecimal("1999.00"), new BigDecimal("-100.00"), "INR", null));
    }

    @Test
    void shouldUpdateDetailsWithValidStrikeOutPrice() {
        Product product = Product.create("SP-SHI-001", "Shiitake Spores", "Desc", new BigDecimal("1999.00"), "INR", null);
        product.updateDetails("Updated Shiitake", "New Desc", new BigDecimal("1799.00"), new BigDecimal("2199.00"), "INR", null);

        assertEquals("Updated Shiitake", product.getName());
        assertEquals(new BigDecimal("1799.00"), product.getPrice());
        assertEquals(new BigDecimal("2199.00"), product.getStrikeOutPrice());
    }

    @Test
    void shouldRejectUpdateDetailsWithInvalidStrikeOutPrice() {
        Product product = Product.create("SP-SHI-001", "Shiitake Spores", "Desc", new BigDecimal("1999.00"), "INR", null);
        assertThrows(IllegalArgumentException.class, () ->
                product.updateDetails("Updated Shiitake", "New Desc", new BigDecimal("1999.00"), new BigDecimal("1500.00"), "INR", null));
    }

    @Test
    void shouldRejectBlankSku() {
        assertThrows(IllegalArgumentException.class, () ->
                Product.create("  ", "Shiitake Spores", "Desc", new BigDecimal("19.99"), "INR", null));
    }

    @Test
    void shouldRejectBlankName() {
        assertThrows(IllegalArgumentException.class, () ->
                Product.create("SP-SHI-001", "", "Desc", new BigDecimal("19.99"), "INR", null));
    }

    @Test
    void shouldRejectNegativePrice() {
        assertThrows(IllegalArgumentException.class, () ->
                Product.create("SP-SHI-001", "Shiitake", "Desc", new BigDecimal("-5.00"), "INR", null));
    }

    @Test
    void shouldAllowLifecycleStatusTransition() {
        Product product = Product.create("SP-SHI-001", "Shiitake Spores", "Desc", new BigDecimal("19.99"), "INR", null);
        product.changeStatus(ProductStatus.ACTIVE);
        assertEquals(ProductStatus.ACTIVE, product.getStatus());

        product.changeStatus(ProductStatus.ARCHIVED);
        assertEquals(ProductStatus.ARCHIVED, product.getStatus());

        assertThrows(IllegalStateException.class, () -> product.changeStatus(ProductStatus.ACTIVE));
    }
}
