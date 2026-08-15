package com.sporekart.modules.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ApiPayloadAndValidationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Should reject requests with unsupported Content-Type on POST endpoints")
    void shouldRejectUnsupportedMediaType() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("user=test&pass=secret"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.error.code").value("UNSUPPORTED_MEDIA_TYPE"));
    }

    @Test
    @DisplayName("Should reject excessive pagination size beyond cap")
    void shouldRejectExcessivePaginationSize() throws Exception {
        mockMvc.perform(get("/api/v1/catalog/products")
                        .param("size", "500"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("CATALOG_INVALID_PAGE_SIZE"));
    }

    @Test
    @DisplayName("Should reject unwhitelisted sort fields")
    void shouldRejectInvalidSortField() throws Exception {
        mockMvc.perform(get("/api/v1/catalog/products")
                        .param("sort", "unauthorizedColumn"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("CATALOG_INVALID_SORT"));
    }

    @Test
    @DisplayName("Should sanitize wildcard overload search queries")
    void shouldSanitizeWildcardSearchOverload() throws Exception {
        mockMvc.perform(get("/api/v1/catalog/products")
                        .param("search", "%%%%%%"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_SEARCH_QUERY"));
    }

    @Test
    @DisplayName("Should reject search query exceeding maximum length")
    void shouldRejectSearchQueryExceedingMaxLength() throws Exception {
        String longQuery = "A".repeat(150);
        mockMvc.perform(get("/api/v1/catalog/products")
                        .param("search", longQuery))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_SEARCH_QUERY"));
    }
}
