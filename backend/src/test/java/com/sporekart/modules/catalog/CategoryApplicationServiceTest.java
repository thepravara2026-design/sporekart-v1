package com.sporekart.modules.catalog;

import com.sporekart.modules.catalog.application.*;
import com.sporekart.modules.catalog.domain.exception.CategoryDeletionException;
import com.sporekart.modules.catalog.domain.exception.DuplicateCategoryException;
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
class CategoryApplicationServiceTest {

    @Autowired
    private CategoryApplicationService categoryApplicationService;

    @Autowired
    private ProductApplicationService productApplicationService;

    @Test
    void shouldCreateAndFetchCategory() {
        CreateCategoryCommand command = new CreateCategoryCommand("Medicinal Spores", "Medicinal mushroom cultures");
        CategoryDto created = categoryApplicationService.createCategory(command);

        assertNotNull(created.id());
        assertEquals("Medicinal Spores", created.name());
        assertEquals("medicinal-spores", created.slug());

        CategoryDto fetched = categoryApplicationService.getCategoryBySlug("medicinal-spores");
        assertEquals(created.id(), fetched.id());
    }

    @Test
    void shouldPreventDuplicateCategoryNames() {
        categoryApplicationService.createCategory(new CreateCategoryCommand("Exotic", "Exotic strains"));
        assertThrows(DuplicateCategoryException.class, () ->
                categoryApplicationService.createCategory(new CreateCategoryCommand("Exotic", "Duplicate strain")));
    }

    @Test
    void shouldPreventCategoryDeletionWhenProductsExist() {
        CategoryDto category = categoryApplicationService.createCategory(new CreateCategoryCommand("Substrates", "Grow media"));
        productApplicationService.createProduct(new CreateProductCommand("SUB-COIR-001", "Coco Coir 5kg", "Sterilized coco coir", new BigDecimal("15.00"), "USD", category.id()));

        assertThrows(CategoryDeletionException.class, () -> categoryApplicationService.deleteCategory(category.id()));
    }
}
