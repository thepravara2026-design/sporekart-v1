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
}
