package com.sporekart.modules.catalog;

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

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CatalogApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductApplicationService productApplicationService;

    @Autowired
    private CategoryApplicationService categoryApplicationService;

    private ProductDto product1;
    private ProductDto product2;
    private CategoryDto category;

    @BeforeEach
    void setUp() {
        category = categoryApplicationService.createCategory(
                new CreateCategoryCommand("Integration Test Category", "Category for integration test")
        );

        product1 = productApplicationService.createProduct(
                new CreateProductCommand("SKU-INT-001", "Integration Product Alpha", "Desc 1", new BigDecimal("10.00"), "USD", category.id())
        );
        productApplicationService.changeProductStatus(product1.id(), ProductStatus.ACTIVE);

        product2 = productApplicationService.createProduct(
                new CreateProductCommand("SKU-INT-002", "Integration Product Beta", "Desc 2", new BigDecimal("20.00"), "USD", category.id())
        );
        productApplicationService.changeProductStatus(product2.id(), ProductStatus.ACTIVE);
    }

    @Test
    void shouldExecuteFullCatalogApiFlowAgainstH2() throws Exception {
        // 1. Fetch categories list
        mockMvc.perform(get("/api/v1/catalog/categories")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(greaterThanOrEqualTo(1)));

        // 2. Fetch category detail by slug
        mockMvc.perform(get("/api/v1/catalog/categories/{categoryId}", category.slug())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(category.id().toString()))
                .andExpect(jsonPath("$.data.name").value("Integration Test Category"));

        // 3. Fetch products list with category filter and sorting
        mockMvc.perform(get("/api/v1/catalog/products")
                        .param("categoryId", category.id().toString())
                        .param("sort", "name,asc")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content.length()").value(2))
                .andExpect(jsonPath("$.data.content[0].name").value("Integration Product Alpha"))
                .andExpect(jsonPath("$.data.content[1].name").value("Integration Product Beta"));

        // 4. Fetch single product by SKU
        mockMvc.perform(get("/api/v1/catalog/products/{productId}", "SKU-INT-001")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.sku").value("SKU-INT-001"))
                .andExpect(jsonPath("$.data.name").value("Integration Product Alpha"));
    }
}
