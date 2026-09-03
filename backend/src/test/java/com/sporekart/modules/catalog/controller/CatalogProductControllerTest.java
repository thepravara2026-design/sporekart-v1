package com.sporekart.modules.catalog.controller;

import com.sporekart.modules.catalog.application.CategoryApplicationService;
import com.sporekart.modules.catalog.application.CategoryDto;
import com.sporekart.modules.catalog.application.CreateCategoryCommand;
import com.sporekart.modules.catalog.application.CreateProductCommand;
import com.sporekart.modules.catalog.application.ProductApplicationService;
import com.sporekart.modules.catalog.application.ProductDto;
import com.sporekart.modules.catalog.domain.product.ProductStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CatalogProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductApplicationService productApplicationService;

    @Autowired
    private CategoryApplicationService categoryApplicationService;

    private ProductDto seededProduct;

    @BeforeEach
    void setUp() {
        CategoryDto category = categoryApplicationService.createCategory(
                new CreateCategoryCommand("Controller Test Category", "Category for controller testing")
        );
        CreateProductCommand command = new CreateProductCommand(
                "SKU-CTRL-001",
                "Controller Test Product",
                "Description for controller test product",
                new BigDecimal("49.99"),
                "INR",
                category.id()
        );
        seededProduct = productApplicationService.createProduct(command);
        productApplicationService.changeProductStatus(seededProduct.id(), ProductStatus.ACTIVE);
    }

    @Test
    void shouldReturnPaginatedProducts() throws Exception {
        mockMvc.perform(get("/api/v1/catalog/products")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "name,asc")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.data.size").value(10))
                .andExpect(jsonPath("$.data.totalElements").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.data.content[?(@.sku == 'SKU-CTRL-001')].name").value("Controller Test Product"));
    }

    @Test
    void shouldFilterProductsByMinAndMaxPrice() throws Exception {
        mockMvc.perform(get("/api/v1/catalog/products")
                        .param("minPrice", "40.00")
                        .param("maxPrice", "60.00")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[?(@.sku == 'SKU-CTRL-001')].price").value(49.99));
    }

    @Test
    void shouldReturn400ForInvalidPriceRange() throws Exception {
        mockMvc.perform(get("/api/v1/catalog/products")
                        .param("minPrice", "100.00")
                        .param("maxPrice", "10.00")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("CATALOG_INVALID_PRICE_RANGE"))
                .andExpect(jsonPath("$.error.message").value(containsString("Minimum price cannot be greater than maximum price")));
    }

    @Test
    void shouldReturnProductById() throws Exception {
        mockMvc.perform(get("/api/v1/catalog/products/{productId}", seededProduct.id())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(seededProduct.id().toString()))
                .andExpect(jsonPath("$.data.sku").value("SKU-CTRL-001"))
                .andExpect(jsonPath("$.data.name").value("Controller Test Product"));
    }

    @Test
    void shouldReturnProductBySku() throws Exception {
        mockMvc.perform(get("/api/v1/catalog/products/{productId}", "SKU-CTRL-001")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.sku").value("SKU-CTRL-001"))
                .andExpect(jsonPath("$.data.name").value("Controller Test Product"));
    }

    @Test
    void shouldReturn404ForNonExistentProduct() throws Exception {
        UUID randomId = UUID.randomUUID();
        mockMvc.perform(get("/api/v1/catalog/products/{productId}", randomId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("CATALOG_PRODUCT_NOT_FOUND"))
                .andExpect(jsonPath("$.error.message").value(containsString(randomId.toString())));
    }

    @Test
    void shouldReturn400ForInvalidPageSize() throws Exception {
        mockMvc.perform(get("/api/v1/catalog/products")
                        .param("size", "150")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("CATALOG_INVALID_PAGE_SIZE"));
    }

    @Test
    void shouldReturn400ForInvalidSortField() throws Exception {
        mockMvc.perform(get("/api/v1/catalog/products")
                        .param("sort", "maliciousField,desc")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("CATALOG_INVALID_SORT"));
    }
}
