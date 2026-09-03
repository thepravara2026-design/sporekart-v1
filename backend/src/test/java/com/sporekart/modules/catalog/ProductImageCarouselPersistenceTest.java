package com.sporekart.modules.catalog;

import com.sporekart.modules.catalog.application.ProductDto;
import com.sporekart.modules.catalog.domain.category.Category;
import com.sporekart.modules.catalog.domain.product.Product;
import com.sporekart.modules.catalog.domain.product.ProductImage;
import com.sporekart.modules.catalog.infrastructure.persistence.CategoryRepository;
import com.sporekart.modules.catalog.infrastructure.persistence.ProductRepository;
import com.sporekart.modules.catalog.application.ProductImageDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FE-PRODUCT-IMAGE-CAROUSEL-01: persistence & mapping tests for the multi-image
 * product carousel feature.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ProductImageCarouselPersistenceTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Test
    @DisplayName("Persistence: product images persist, are ordered by displayOrder and surfaced on read")
    void shouldPersistOrderedProductImages() {
        Category category = categoryRepository.save(Category.create("Images", "Category for image tests"));
        Product product = Product.create("IMG-PROD-001", "Image Product", "Product with images", new BigDecimal("10.00"), "INR", category);
        product.setImages(ProductImage.listFromUrls(product.getId(), List.of(
                "https://cdn.example.com/a.jpg",
                "https://cdn.example.com/b.jpg",
                "https://cdn.example.com/c.jpg"
        )));

        Product saved = productRepository.save(product);
        assertNotNull(saved.getId());
        assertEquals(3, saved.getImages().size());

        Optional<Product> fetched = productRepository.findById(saved.getId());
        assertTrue(fetched.isPresent());
        assertEquals(3, fetched.get().getImages().size());
        assertEquals("https://cdn.example.com/a.jpg", fetched.get().getImageUrl());
        assertEquals("https://cdn.example.com/a.jpg", fetched.get().getImages().get(0).getImageUrl());
        assertEquals(1, fetched.get().getImages().get(0).getDisplayOrder());
        assertEquals("https://cdn.example.com/c.jpg", fetched.get().getImages().get(2).getImageUrl());
        assertEquals(3, fetched.get().getImages().get(2).getDisplayOrder());
    }

    @Test
    @DisplayName("Persistence: clearing the image set removes all product images")
    void shouldClearProductImages() {
        Category category = categoryRepository.save(Category.create("Clear", "Clear images"));
        Product product = Product.create("IMG-CLR-001", "Clear Product", "desc", new BigDecimal("5.00"), "INR", category);
        product.setImages(ProductImage.listFromUrls(product.getId(), List.of("https://cdn.example.com/x.jpg")));
        Product saved = productRepository.save(product);
        assertEquals(1, saved.getImages().size());

        saved.setImages(List.of());
        Product cleared = productRepository.save(saved);
        assertTrue(cleared.getImages().isEmpty());
        assertNull(cleared.getImageUrl());
    }

    @Test
    @DisplayName("Domain: maximum four images enforced")
    void shouldRejectMoreThanFourImages() {
        Product product = Product.create("IMG-MAX-001", "Max Images", "desc", new BigDecimal("5.00"), "INR", null);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                ProductImage.listFromUrls(product.getId(), List.of(
                        "https://cdn.example.com/1.jpg",
                        "https://cdn.example.com/2.jpg",
                        "https://cdn.example.com/3.jpg",
                        "https://cdn.example.com/4.jpg",
                        "https://cdn.example.com/5.jpg"
                ))
        );
        assertTrue(ex.getMessage().contains("at most 4"));
    }

    @Test
    @DisplayName("Domain: duplicate display order rejected")
    void shouldRejectDuplicateDisplayOrder() {
        Product product = Product.create("IMG-DUP-001", "Dup Order", "desc", new BigDecimal("5.00"), "INR", null);
        ProductImage a = ProductImage.create(product.getId(), "https://cdn.example.com/a.jpg", 1);
        ProductImage b = ProductImage.create(product.getId(), "https://cdn.example.com/b.jpg", 1);
        product.addImage(a);
        assertThrows(IllegalArgumentException.class, () -> product.addImage(b));
    }

    @Test
    @DisplayName("Domain: blank image URL rejected")
    void shouldRejectBlankImageUrl() {
        assertThrows(IllegalArgumentException.class, () ->
                ProductImage.create(null, "   ", 1)
        );
    }

    @Test
    @DisplayName("DTO: fromDomain maps ordered images and primary imageUrl")
    void shouldMapImagesToDto() {
        Category category = Category.create("Dto", "DTO category");
        Product product = Product.create("IMG-DTO-001", "Dto Product", "desc", new BigDecimal("5.00"), "INR", category);
        product.setImages(ProductImage.listFromUrls(product.getId(), List.of(
                "https://cdn.example.com/first.jpg",
                "https://cdn.example.com/second.jpg"
        )));

        ProductDto dto = ProductDto.fromDomain(product);

        assertEquals("https://cdn.example.com/first.jpg", dto.imageUrl());
        assertNotNull(dto.images());
        assertEquals(2, dto.images().size());
        assertEquals(ProductImageDto.class, dto.images().get(0).getClass());
        assertEquals("first.jpg", dto.images().get(0).imageUrl().substring(dto.images().get(0).imageUrl().lastIndexOf('/') + 1));
        assertEquals(2, dto.images().get(1).displayOrder());
    }
}