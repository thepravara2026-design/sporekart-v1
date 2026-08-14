package com.sporekart.modules.catalog;

import com.sporekart.modules.catalog.application.*;
import com.sporekart.modules.catalog.domain.exception.DuplicateSkuException;
import com.sporekart.modules.catalog.domain.product.ProductStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ProductApplicationServiceTest {

    @Autowired
    private ProductApplicationService productApplicationService;

    @Autowired
    private CategoryApplicationService categoryApplicationService;

    @Test
    void shouldCreateProductAndTransitionLifecycleStatus() {
        CategoryDto category = categoryApplicationService.createCategory(new CreateCategoryCommand("Liquid Cultures", "Ready to inoculate LC syringe"));

        CreateProductCommand command = new CreateProductCommand(
                "sp-lkm-001",
                "Lions Mane Liquid Culture",
                "10ml Lions Mane culture syringe",
                new BigDecimal("22.50"),
                "USD",
                category.id()
        );

        ProductDto product = productApplicationService.createProduct(command);

        assertNotNull(product.id());
        assertEquals("SP-LKM-001", product.sku());
        assertEquals(ProductStatus.DRAFT, product.status());
        assertEquals("Liquid Cultures", product.category().name());

        ProductDto activated = productApplicationService.changeProductStatus(product.id(), ProductStatus.ACTIVE);
        assertEquals(ProductStatus.ACTIVE, activated.status());
    }

    @Test
    void shouldPreventDuplicateSkuCreation() {
        CreateProductCommand command1 = new CreateProductCommand("SP-REI-001", "Reishi Spores", "Desc", new BigDecimal("30.00"), "USD", null);
        productApplicationService.createProduct(command1);

        CreateProductCommand command2 = new CreateProductCommand("sp-rei-001", "Reishi Liquid Culture", "Desc 2", new BigDecimal("35.00"), "USD", null);
        assertThrows(DuplicateSkuException.class, () -> productApplicationService.createProduct(command2));
    }

    @Test
    void shouldGetPaginatedProductsWithValidation() {
        CreateProductCommand command1 = new CreateProductCommand("SKU-PAG-001", "Alpha Product", "Desc", new BigDecimal("10.00"), "USD", null);
        CreateProductCommand command2 = new CreateProductCommand("SKU-PAG-002", "Beta Product", "Desc", new BigDecimal("20.00"), "USD", null);
        productApplicationService.createProduct(command1);
        productApplicationService.createProduct(command2);

        var pagedResponse = productApplicationService.getProducts(0, 10, "name,asc", null, null, null);
        assertNotNull(pagedResponse);
        assertTrue(pagedResponse.content().size() >= 2);

        assertThrows(IllegalArgumentException.class, () -> productApplicationService.getProducts(-1, 10, "name,asc", null, null, null));
        assertThrows(IllegalArgumentException.class, () -> productApplicationService.getProducts(0, 0, "name,asc", null, null, null));
        assertThrows(IllegalArgumentException.class, () -> productApplicationService.getProducts(0, 101, "name,asc", null, null, null));
        assertThrows(IllegalArgumentException.class, () -> productApplicationService.getProducts(0, 10, "invalidField,asc", null, null, null));
    }

    @Test
    void shouldFilterProductsByPriceRange() {
        CreateProductCommand p1 = new CreateProductCommand("SKU-PR-001", "Cheap Spores", "Desc", new BigDecimal("15.00"), "USD", null);
        CreateProductCommand p2 = new CreateProductCommand("SKU-PR-002", "Mid Spores", "Desc", new BigDecimal("45.00"), "USD", null);
        CreateProductCommand p3 = new CreateProductCommand("SKU-PR-003", "Expensive Spores", "Desc", new BigDecimal("95.00"), "USD", null);
        productApplicationService.createProduct(p1);
        productApplicationService.createProduct(p2);
        productApplicationService.createProduct(p3);

        // Filter minPrice=20, maxPrice=60 -> should find p2 (45.00)
        var filtered = productApplicationService.getProducts(0, 10, "price,asc", null, null, null, new BigDecimal("20.00"), new BigDecimal("60.00"));
        assertNotNull(filtered);
        assertEquals(1, filtered.content().size());
        assertEquals("SKU-PR-002", filtered.content().get(0).sku());
    }

    @Test
    void shouldValidatePriceRangeInputs() {
        assertThrows(IllegalArgumentException.class, () ->
                productApplicationService.getProducts(0, 10, null, null, null, null, new BigDecimal("-5.00"), null));

        assertThrows(IllegalArgumentException.class, () ->
                productApplicationService.getProducts(0, 10, null, null, null, null, null, new BigDecimal("-10.00")));

        assertThrows(IllegalArgumentException.class, () ->
                productApplicationService.getProducts(0, 10, null, null, null, null, new BigDecimal("100.00"), new BigDecimal("50.00")));
    }
}
