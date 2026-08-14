package com.sporekart.modules.catalog.controller;

import com.sporekart.modules.catalog.application.CategoryApplicationService;
import com.sporekart.modules.catalog.application.CategoryDto;
import com.sporekart.modules.catalog.application.CreateCategoryCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CatalogCategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CategoryApplicationService categoryApplicationService;

    private CategoryDto seededCategory;

    @BeforeEach
    void setUp() {
        seededCategory = categoryApplicationService.createCategory(
                new CreateCategoryCommand("Controller Category", "Description for controller category test")
        );
    }

    @Test
    void shouldReturnPaginatedCategories() throws Exception {
        mockMvc.perform(get("/api/v1/catalog/categories")
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
                .andExpect(jsonPath("$.data.content[?(@.slug == 'controller-category')].name").value("Controller Category"));
    }

    @Test
    void shouldReturnCategoryById() throws Exception {
        mockMvc.perform(get("/api/v1/catalog/categories/{categoryId}", seededCategory.id())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(seededCategory.id().toString()))
                .andExpect(jsonPath("$.data.name").value("Controller Category"))
                .andExpect(jsonPath("$.data.slug").value("controller-category"));
    }

    @Test
    void shouldReturnCategoryBySlug() throws Exception {
        mockMvc.perform(get("/api/v1/catalog/categories/{categoryId}", "controller-category")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Controller Category"))
                .andExpect(jsonPath("$.data.slug").value("controller-category"));
    }

    @Test
    void shouldReturn404ForNonExistentCategory() throws Exception {
        UUID randomId = UUID.randomUUID();
        mockMvc.perform(get("/api/v1/catalog/categories/{categoryId}", randomId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("CATALOG_CATEGORY_NOT_FOUND"));
    }

    @Test
    void shouldReturn400ForInvalidPageSize() throws Exception {
        mockMvc.perform(get("/api/v1/catalog/categories")
                        .param("size", "0")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("CATALOG_INVALID_PAGE_SIZE"));
    }

    @Test
    void shouldReturn400ForInvalidSortField() throws Exception {
        mockMvc.perform(get("/api/v1/catalog/categories")
                        .param("sort", "nonExistentField,asc")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("CATALOG_INVALID_SORT"));
    }
}
