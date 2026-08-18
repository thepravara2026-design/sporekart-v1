package com.sporekart.modules.catalog.domain;

import com.sporekart.modules.catalog.domain.category.Category;
import com.sporekart.modules.catalog.domain.category.CategoryStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CategoryDomainTest {

    @Test
    void shouldCreateCategoryWithSlugGeneration() {
        Category category = Category.create("Gourmet Mushrooms & Spores", "All gourmet varieties");

        assertNotNull(category.getId());
        assertEquals("Gourmet Mushrooms & Spores", category.getName());
        assertEquals("gourmet-mushrooms-spores", category.getSlug());
        assertEquals(CategoryStatus.ACTIVE, category.getStatus());
    }

    @Test
    void shouldRejectBlankCategoryName() {
        assertThrows(IllegalArgumentException.class, () -> Category.create("  ", "Description"));
    }

    @Test
    void shouldUpdateCategoryDetails() {
        Category category = Category.create("Medicinal", "Old desc");
        category.update("Medicinal Mushrooms", "New desc", CategoryStatus.INACTIVE);

        assertEquals("Medicinal Mushrooms", category.getName());
        assertEquals("medicinal-mushrooms", category.getSlug());
        assertEquals("New desc", category.getDescription());
        assertEquals(CategoryStatus.INACTIVE, category.getStatus());
    }
}
