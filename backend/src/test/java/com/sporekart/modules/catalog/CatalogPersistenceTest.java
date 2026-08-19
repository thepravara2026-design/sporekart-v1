package com.sporekart.modules.catalog;

import com.sporekart.modules.catalog.domain.category.Category;
import com.sporekart.modules.catalog.domain.product.Product;
import com.sporekart.modules.catalog.domain.product.ProductStatus;
import com.sporekart.modules.catalog.infrastructure.persistence.CategoryRepository;
import com.sporekart.modules.catalog.infrastructure.persistence.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CatalogPersistenceTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Test
    void shouldPersistAndRetrieveCategoryAndProduct() {
        Category category = Category.create("Gourmet", "Gourmet mushroom strain");
        Category savedCategory = categoryRepository.save(category);

        assertNotNull(savedCategory.getId());
        assertTrue(categoryRepository.existsBySlug("gourmet"));

        Product product = Product.create("SP-OYST-001", "Blue Oyster Spores", "Fresh Blue Oyster liquid culture", new BigDecimal("24.99"), "INR", savedCategory);
        Product savedProduct = productRepository.save(product);

        assertNotNull(savedProduct.getId());
        assertTrue(productRepository.existsBySku("SP-OYST-001"));

        Optional<Product> fetchedProduct = productRepository.findBySku("SP-OYST-001");
        assertTrue(fetchedProduct.isPresent());
        assertEquals("Blue Oyster Spores", fetchedProduct.get().getName());
        assertEquals(savedCategory.getId(), fetchedProduct.get().getCategory().getId());
        assertEquals(1L, productRepository.countByCategoryId(savedCategory.getId()));
    }
}
