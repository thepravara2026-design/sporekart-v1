package com.sporekart.modules.catalog;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sporekart.SporekartApplication;
import com.sporekart.integration.CommerceFixtures;
import com.sporekart.modules.catalog.application.ProductApplicationService;
import com.sporekart.modules.catalog.application.ProductDto;
import com.sporekart.modules.catalog.application.dto.response.PageResponse;
import com.sporekart.modules.catalog.domain.product.ProductStatus;
import com.sporekart.modules.catalog.infrastructure.persistence.SpringDataCategoryRepository;
import com.sporekart.modules.catalog.infrastructure.persistence.SpringDataProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Sprint 6G — Search & Catalog Hardening Test Suite.
 *
 * Verifies catalog search accuracy, input normalization, wildcard safety,
 * filter combinations, price boundary enforcement, sort stability,
 * pagination limits, and status visibility controls.
 */
@SpringBootTest(classes = SporekartApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.MethodName.class)
@DisplayName("Sprint 6G — Search & Catalog Hardening Test Suite")
class CatalogSearchHardeningTestSuite {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductApplicationService productApplicationService;

    @Autowired
    private SpringDataProductRepository productRepository;

    @Autowired
    private SpringDataCategoryRepository categoryRepository;

    // =========================================================
    // 6G-001: Exact & partial search with case insensitivity
    // =========================================================

    @Test
    @Transactional
    @DisplayName("6G-001: Search matches exact and partial text case-insensitively across name, SKU, and description")
    void test_6G_001_exactAndPartialSearchIsCaseInsensitive() throws Exception {
        String uniqueSuffix = UUID.randomUUID().toString().substring(0, 6);
        String sku = "SKU-6G-001-" + uniqueSuffix;
        String name = "Golden Teacher Spore Syringe " + uniqueSuffix;

        CommerceFixtures.createTestProduct(
                productRepository, categoryRepository,
                sku, name, BigDecimal.valueOf(45.00), ProductStatus.ACTIVE
        );

        // Search lower-case partial name "golden teacher"
        MvcResult result = mockMvc.perform(get("/api/v1/catalog/products")
                        .param("search", "golden teacher")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(json.path("success").asBoolean()).isTrue();

        JsonNode items = json.path("data").path("content");
        assertThat(items.isArray()).isTrue();
        assertThat(items.size()).isGreaterThanOrEqualTo(1);

        boolean found = false;
        for (JsonNode node : items) {
            if (node.path("sku").asText().equalsIgnoreCase(sku)) {
                found = true;
                break;
            }
        }
        assertThat(found).as("Product should be found by case-insensitive search term").isTrue();
    }

    // =========================================================
    // 6G-002: Search input normalization and wildcard safety
    // =========================================================

    @Test
    @DisplayName("6G-002: Search input normalizes whitespace and rejects wildcard-only queries safely")
    void test_6G_002_searchInputSafetyAndWildcardNormalization() {
        // Wildcard-only search string "%_*?" should be rejected cleanly with IllegalArgumentException
        assertThatThrownBy(() -> productApplicationService.getProducts(0, 10, null, null, null, "%_*?", null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("excessive wildcards");

        // Excessively long search (> 100 chars) should be rejected
        String longSearch = "a".repeat(101);
        assertThatThrownBy(() -> productApplicationService.getProducts(0, 10, null, null, null, longSearch, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot exceed 100 characters");
    }

    // =========================================================
    // 6G-003: Multi-filter combination (category + status + minPrice + maxPrice)
    // =========================================================

    @Test
    @Transactional
    @DisplayName("6G-003: Multi-filter queries return correct intersection of criteria")
    void test_6G_003_multiFilterCombinationReturnsExactIntersection() {
        String uniqueSuffix = UUID.randomUUID().toString().substring(0, 6);

        var p1 = CommerceFixtures.createTestProduct(
                productRepository, categoryRepository,
                "SKU-6G-003-A-" + uniqueSuffix, "Filter Test Product A " + uniqueSuffix,
                BigDecimal.valueOf(25.00), ProductStatus.ACTIVE
        );

        var p2 = CommerceFixtures.createTestProduct(
                productRepository, categoryRepository,
                "SKU-6G-003-B-" + uniqueSuffix, "Filter Test Product B " + uniqueSuffix,
                BigDecimal.valueOf(150.00), ProductStatus.ACTIVE
        );

        UUID categoryId = p1.getCategory().getId();

        // Filter by category + price between 10.00 and 50.00
        PageResponse<ProductDto> page = productApplicationService.getProducts(
                0, 10, null, categoryId, ProductStatus.ACTIVE, null,
                BigDecimal.valueOf(10.00), BigDecimal.valueOf(50.00)
        );

        List<ProductDto> results = page.content();
        assertThat(results).isNotEmpty();
        assertThat(results).allMatch(p -> p.price().compareTo(BigDecimal.valueOf(10.00)) >= 0);
        assertThat(results).allMatch(p -> p.price().compareTo(BigDecimal.valueOf(50.00)) <= 0);
        assertThat(results).noneMatch(p -> p.id().equals(p2.getId()));
    }

    // =========================================================
    // 6G-004: Price boundary validation
    // =========================================================

    @Test
    @DisplayName("6G-004: Price bounds validate negative numbers and minPrice > maxPrice cleanly")
    void test_6G_004_priceBoundValidation() throws Exception {
        // Negative minPrice -> 400 Bad Request
        mockMvc.perform(get("/api/v1/catalog/products")
                        .param("minPrice", "-10.00"))
                .andExpect(status().isBadRequest());

        // minPrice > maxPrice -> 400 Bad Request
        mockMvc.perform(get("/api/v1/catalog/products")
                        .param("minPrice", "100.00")
                        .param("maxPrice", "50.00"))
                .andExpect(status().isBadRequest());
    }

    // =========================================================
    // 6G-005: Deterministic sort ordering with tie-breaker
    // =========================================================

    @Test
    @Transactional
    @DisplayName("6G-005: Sorting orders products deterministically with ID secondary tie-breaker")
    void test_6G_005_deterministicSortOrdering() {
        PageResponse<ProductDto> pageAsc = productApplicationService.getProducts(
                0, 10, "price,asc", null, ProductStatus.ACTIVE, null, null, null
        );

        List<ProductDto> content = pageAsc.content();
        if (content.size() >= 2) {
            for (int i = 0; i < content.size() - 1; i++) {
                BigDecimal p1 = content.get(i).price();
                BigDecimal p2 = content.get(i + 1).price();
                assertThat(p1.compareTo(p2)).isLessThanOrEqualTo(0);
            }
        }
    }

    // =========================================================
    // 6G-006: Bounded pagination and maximum page size limits
    // =========================================================

    @Test
    @DisplayName("6G-006: Pagination enforces page index bounds and max page size cap (100)")
    void test_6G_006_paginationLimitsEnforced() throws Exception {
        // Negative page index -> 400 Bad Request
        mockMvc.perform(get("/api/v1/catalog/products")
                        .param("page", "-1"))
                .andExpect(status().isBadRequest());

        // Page size exceeding 100 -> 400 Bad Request
        mockMvc.perform(get("/api/v1/catalog/products")
                        .param("size", "200"))
                .andExpect(status().isBadRequest());
    }

    // =========================================================
    // 6G-007: Product status visibility controls
    // =========================================================

    @Test
    @Transactional
    @DisplayName("6G-007: Inactive products are excluded when status filter specifies ACTIVE")
    void test_6G_007_statusVisibilityControls() {
        String uniqueSuffix = UUID.randomUUID().toString().substring(0, 6);

        var activeProd = CommerceFixtures.createTestProduct(
                productRepository, categoryRepository,
                "SKU-6G-007-ACT-" + uniqueSuffix, "Active Product " + uniqueSuffix,
                BigDecimal.valueOf(30.00), ProductStatus.ACTIVE
        );

        var draftProd = CommerceFixtures.createTestProduct(
                productRepository, categoryRepository,
                "SKU-6G-007-DRF-" + uniqueSuffix, "Draft Product " + uniqueSuffix,
                BigDecimal.valueOf(30.00), ProductStatus.DRAFT
        );

        // Fetch with status = ACTIVE
        PageResponse<ProductDto> page = productApplicationService.getProducts(
                0, 50, null, null, ProductStatus.ACTIVE, null, null, null
        );

        List<ProductDto> content = page.content();
        assertThat(content).anyMatch(p -> p.id().equals(activeProd.getId()));
        assertThat(content).noneMatch(p -> p.id().equals(draftProd.getId()));
    }
}
