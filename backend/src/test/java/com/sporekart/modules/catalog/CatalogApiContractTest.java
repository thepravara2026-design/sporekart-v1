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
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CatalogApiContractTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductApplicationService productApplicationService;

    @Autowired
    private CategoryApplicationService categoryApplicationService;

    private ProductDto testProduct;

    @BeforeEach
    void setUp() {
        CategoryDto category = categoryApplicationService.createCategory(
                new CreateCategoryCommand("Contract Test Category", "Category for API contract testing")
        );
        CreateProductCommand command = new CreateProductCommand(
                "SKU-CONTRACT-001",
                "Contract Test Mushroom",
                "Organic mushroom for API contract verification",
                new BigDecimal("39.99"),
                "USD",
                category.id()
        );
        testProduct = productApplicationService.createProduct(command);
        productApplicationService.changeProductStatus(testProduct.id(), ProductStatus.ACTIVE);
    }

    @Test
    void shouldServeOpenApiV3Specification() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.openapi", startsWith("3.")))
                .andExpect(jsonPath("$.info.title").value("Sporekart v3.0 REST API"))
                .andExpect(jsonPath("$.paths['/api/v1/catalog/products']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/catalog/categories']").exists());
    }

    @Test
    void shouldConformToProductListingContract() throws Exception {
        mockMvc.perform(get("/api/v1/catalog/products")
                        .param("search", "SKU-CONTRACT-001")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "price,desc")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.page").isNumber())
                .andExpect(jsonPath("$.data.size").isNumber())
                .andExpect(jsonPath("$.data.totalElements").isNumber())
                .andExpect(jsonPath("$.data.totalPages").isNumber())
                .andExpect(jsonPath("$.data.first").isBoolean())
                .andExpect(jsonPath("$.data.last").isBoolean())
                .andExpect(jsonPath("$.data.content[0].id").exists())
                .andExpect(jsonPath("$.data.content[0].sku").exists())
                .andExpect(jsonPath("$.data.content[0].name").exists())
                .andExpect(jsonPath("$.data.content[0].price").isNumber())
                .andExpect(jsonPath("$.data.content[0].currency").value("USD"))
                .andExpect(jsonPath("$.data.content[0].status").exists());
    }

    @Test
    void shouldConformToCategoryListingContract() throws Exception {
        mockMvc.perform(get("/api/v1/catalog/categories")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].id").exists())
                .andExpect(jsonPath("$.data.content[0].name").exists())
                .andExpect(jsonPath("$.data.content[0].slug").exists());
    }

    @Test
    void shouldConformToErrorContractForMissingProduct() throws Exception {
        UUID randomId = UUID.randomUUID();
        mockMvc.perform(get("/api/v1/catalog/products/{productId}", randomId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("CATALOG_PRODUCT_NOT_FOUND"))
                .andExpect(jsonPath("$.error.message").exists())
                .andExpect(jsonPath("$.error.timestamp").exists())
                .andExpect(jsonPath("$.error.path").value("/api/v1/catalog/products/" + randomId));
    }

    @Test
    void shouldConformToErrorContractForInvalidPageSize() throws Exception {
        mockMvc.perform(get("/api/v1/catalog/products")
                        .param("size", "500")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("CATALOG_INVALID_PAGE_SIZE"))
                .andExpect(jsonPath("$.error.message").value(containsString("Page size cannot exceed maximum limit")));
    }
}
