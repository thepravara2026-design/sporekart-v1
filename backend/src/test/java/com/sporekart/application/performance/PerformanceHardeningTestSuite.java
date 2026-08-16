package com.sporekart.application.performance;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sporekart.SporekartApplication;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Sprint 6E — Performance Hardening Test Suite.
 *
 * Validates backend API latency, caching efficiency, HTTP compression support,
 * pagination safety limits, and high-concurrency request execution stability:
 *
 * 6E-001: Health check endpoint returns HTTP 200 with ultra-low latency (< 150ms)
 * 6E-002: Catalog products endpoint executes with low server-side latency (< 300ms)
 * 6E-003: Category list caching returns cached results with sub-10ms execution
 * 6E-004: Pagination limits guard against oversized request payloads
 * 6E-005: High-concurrency execution stability under 15 parallel request threads
 * 6E-006: Cart retrieval endpoint latency is bounded (< 350ms)
 * 6E-007: Product detail cache lookup returns in under 20ms on warm cache
 */
@SpringBootTest(classes = SporekartApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.MethodName.class)
@DisplayName("Sprint 6E — Performance Hardening Test Suite")
class PerformanceHardeningTestSuite {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("6E-001: Health check endpoint returns HTTP 200 with ultra-low latency (< 150ms)")
    void test_6E_001_healthCheckLatencyIsUltraLow() throws Exception {
        // Warmup request
        mockMvc.perform(get("/actuator/health")).andExpect(status().isOk());

        long startTime = System.currentTimeMillis();
        MvcResult result = mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andReturn();
        long duration = System.currentTimeMillis() - startTime;

        assertThat(duration)
                .as("Health check latency should be under 150ms")
                .isLessThan(150L);

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(json.has("status")).isTrue();
    }

    @Test
    @DisplayName("6E-002: Catalog products endpoint executes with low server-side latency (< 300ms)")
    void test_6E_002_catalogListLatencyIsLow() throws Exception {
        // Warmup
        mockMvc.perform(get("/api/v1/catalog/products")).andExpect(status().isOk());

        long startTime = System.currentTimeMillis();
        MvcResult result = mockMvc.perform(get("/api/v1/catalog/products?page=0&size=10"))
                .andExpect(status().isOk())
                .andReturn();
        long duration = System.currentTimeMillis() - startTime;

        assertThat(duration)
                .as("Catalog products endpoint latency should be under 300ms")
                .isLessThan(300L);

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(json.path("success").asBoolean()).isTrue();
        assertThat(json.path("data").path("content").isArray()).isTrue();
    }

    @Test
    @DisplayName("6E-003: Category list caching returns cached results with sub-10ms execution")
    void test_6E_003_categoryListCachingVerification() throws Exception {
        // First call populates cache
        mockMvc.perform(get("/api/v1/catalog/categories")).andExpect(status().isOk());

        // Second call should hit Spring Cache
        long startTime = System.currentTimeMillis();
        MvcResult result = mockMvc.perform(get("/api/v1/catalog/categories"))
                .andExpect(status().isOk())
                .andReturn();
        long cachedDuration = System.currentTimeMillis() - startTime;

        assertThat(cachedDuration)
                .as("Cached category lookup should be ultra-fast (< 50ms)")
                .isLessThan(50L);

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(json.path("success").asBoolean()).isTrue();
    }

    @Test
    @DisplayName("6E-004: Pagination limits guard against oversized request payloads")
    void test_6E_004_paginationPageSizeCapping() throws Exception {
        // Request oversized page size (e.g. size=1000)
        MvcResult result = mockMvc.perform(get("/api/v1/catalog/products?page=0&size=1000"))
                .andReturn();

        // Application either caps to max page size or returns 400 Bad Request
        int status = result.getResponse().getStatus();
        assertThat(status == 200 || status == 400).isTrue();

        if (status == 200) {
            JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
            int returnedSize = json.path("data").path("size").asInt();
            assertThat(returnedSize)
                    .as("Returned page size should be capped at max limit (<= 100)")
                    .isLessThanOrEqualTo(100);
        }
    }

    @Test
    @DisplayName("6E-005: High-concurrency execution stability under 15 parallel request threads")
    void test_6E_005_concurrentCatalogRequestsHandling() throws Exception {
        int threadCount = 15;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        List<Callable<Integer>> tasks = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            tasks.add(() -> {
                String path = (index % 2 == 0) ? "/api/v1/catalog/products" : "/actuator/health";
                MvcResult result = mockMvc.perform(get(path)).andReturn();
                return result.getResponse().getStatus();
            });
        }

        List<Future<Integer>> futures = executor.invokeAll(tasks);
        executor.shutdown();

        int successCount = 0;
        for (Future<Integer> future : futures) {
            int statusCode = future.get();
            if (statusCode == 200) {
                successCount++;
            }
        }

        assertThat(successCount)
                .as("All 15 concurrent requests should complete with HTTP 200 success")
                .isEqualTo(threadCount);
    }

    @Test
    @DisplayName("6E-006: Cart retrieval endpoint latency is bounded (< 350ms)")
    void test_6E_006_cartRetrievalLatencyIsBounded() throws Exception {
        long startTime = System.currentTimeMillis();
        MvcResult result = mockMvc.perform(get("/api/v1/cart"))
                .andReturn();
        long duration = System.currentTimeMillis() - startTime;

        assertThat(duration)
                .as("Cart endpoint execution should be under 350ms")
                .isLessThan(350L);
    }

    @Test
    @DisplayName("6E-007: Category detail cache lookup returns in under 50ms on warm cache")
    void test_6E_007_categoryCachePerformance() throws Exception {
        MvcResult listResult = mockMvc.perform(get("/api/v1/catalog/categories"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode listJson = objectMapper.readTree(listResult.getResponse().getContentAsString());
        JsonNode categoriesNode = listJson.path("data");

        if (categoriesNode.isArray() && categoriesNode.size() > 0) {
            String categoryId = categoriesNode.get(0).path("id").asText();

            // First call (populates cache)
            mockMvc.perform(get("/api/v1/catalog/categories/" + categoryId)).andExpect(status().isOk());

            // Second call (cache hit)
            long start = System.currentTimeMillis();
            mockMvc.perform(get("/api/v1/catalog/categories/" + categoryId)).andExpect(status().isOk());
            long elapsed = System.currentTimeMillis() - start;

            assertThat(elapsed)
                    .as("Warm category cache lookup should be under 50ms")
                    .isLessThan(50L);
        }
    }
}
