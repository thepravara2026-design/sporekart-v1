package com.sporekart.modules.catalog.domain;

import com.sporekart.modules.catalog.domain.product.Product;
import com.sporekart.modules.catalog.domain.product.ProductStatus;
import com.sporekart.modules.catalog.domain.product.ProductVariant;
import com.sporekart.modules.catalog.domain.product.QuantityUnit;
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
    void shouldSupportMultiUnitProductVariants() {
        Product product = Product.create("SP-OYSTER-001", "Oyster Mushroom", "Fresh harvest", new BigDecimal("100.00"), "INR", null);

        ProductVariant v100g = ProductVariant.create(product.getId(), "SP-OYSTER-100G", new BigDecimal("100"), QuantityUnit.G, new BigDecimal("60.00"), new BigDecimal("80.00"));
        ProductVariant v250g = ProductVariant.create(product.getId(), "SP-OYSTER-250G", new BigDecimal("250"), QuantityUnit.G, new BigDecimal("130.00"), new BigDecimal("160.00"));
        ProductVariant v500g = ProductVariant.create(product.getId(), "SP-OYSTER-500G", new BigDecimal("500"), QuantityUnit.G, new BigDecimal("240.00"), null);
        ProductVariant v1kg = ProductVariant.create(product.getId(), "SP-OYSTER-1KG", new BigDecimal("1"), QuantityUnit.KG, new BigDecimal("450.00"), new BigDecimal("500.00"));

        product.addVariant(v100g);
        product.addVariant(v250g);
        product.addVariant(v500g);
        product.addVariant(v1kg);

        assertEquals(4, product.getVariants().size());
        assertEquals("100 g", v100g.getFormattedQuantity());
        assertEquals("250 g", v250g.getFormattedQuantity());
        assertEquals("500 g", v500g.getFormattedQuantity());
        assertEquals("1 kg", v1kg.getFormattedQuantity());
        assertEquals(new BigDecimal("60.00"), product.getPrice(), "Base price should update to minimum variant price");
    }

    @Test
    void shouldSupportLiquidVariantsInMillilitersAndLiters() {
        Product product = Product.create("SP-EXTRACT-001", "Mushroom Extract", "Liquid extract", new BigDecimal("500.00"), "INR", null);

        ProductVariant v100ml = ProductVariant.create(product.getId(), "SP-EXTRACT-100ML", new BigDecimal("100"), QuantityUnit.ML, new BigDecimal("499.00"), new BigDecimal("599.00"));
        ProductVariant v1L = ProductVariant.create(product.getId(), "SP-EXTRACT-1L", new BigDecimal("1"), QuantityUnit.L, new BigDecimal("2999.00"), null);

        product.addVariant(v100ml);
        product.addVariant(v1L);

        assertEquals(2, product.getVariants().size());
        assertEquals("100 ml", v100ml.getFormattedQuantity());
        assertEquals("1 l", v1L.getFormattedQuantity());
    }

    @Test
    void shouldRejectDuplicateVariantQuantityOnSameProduct() {
        Product product = Product.create("SP-OYSTER-001", "Oyster Mushroom", "Fresh harvest", new BigDecimal("100.00"), "INR", null);

        ProductVariant v1 = ProductVariant.create(product.getId(), "SP-OYSTER-100G-A", new BigDecimal("100"), QuantityUnit.G, new BigDecimal("60.00"), null);
        ProductVariant v2 = ProductVariant.create(product.getId(), "SP-OYSTER-100G-B", new BigDecimal("100"), QuantityUnit.G, new BigDecimal("65.00"), null);

        product.addVariant(v1);
        assertThrows(IllegalArgumentException.class, () -> product.addVariant(v2));
    }

    @Test
    void shouldRejectVariantWithZeroOrNegativeQuantity() {
        assertThrows(IllegalArgumentException.class, () ->
                ProductVariant.create(java.util.UUID.randomUUID(), "SKU-TEST", BigDecimal.ZERO, QuantityUnit.G, new BigDecimal("50.00"), null));

        assertThrows(IllegalArgumentException.class, () ->
                ProductVariant.create(java.util.UUID.randomUUID(), "SKU-TEST", new BigDecimal("-10"), QuantityUnit.G, new BigDecimal("50.00"), null));
    }

    @Test
    void shouldRejectVariantStrikeOutPriceLessThanOrEqualToSellingPrice() {
        assertThrows(IllegalArgumentException.class, () ->
                ProductVariant.create(java.util.UUID.randomUUID(), "SKU-TEST", new BigDecimal("100"), QuantityUnit.G, new BigDecimal("100.00"), new BigDecimal("100.00")));

        assertThrows(IllegalArgumentException.class, () ->
                ProductVariant.create(java.util.UUID.randomUUID(), "SKU-TEST", new BigDecimal("100"), QuantityUnit.G, new BigDecimal("100.00"), new BigDecimal("90.00")));
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
