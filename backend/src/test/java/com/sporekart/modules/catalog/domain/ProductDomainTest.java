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
        assertEquals(ProductStatus.DRAFT, product.getStatus());
        assertNotNull(product.getCreatedAt());
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
