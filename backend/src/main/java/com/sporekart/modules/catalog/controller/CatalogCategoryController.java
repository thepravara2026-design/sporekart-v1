package com.sporekart.modules.catalog.controller;

import com.sporekart.application.exception.ApiResponse;
import com.sporekart.modules.catalog.application.CategoryApplicationService;
import com.sporekart.modules.catalog.application.CategoryDto;
import com.sporekart.modules.catalog.application.dto.response.PageResponse;
import com.sporekart.modules.catalog.domain.category.CategoryStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/catalog/categories")
public class CatalogCategoryController {

    private final CategoryApplicationService categoryApplicationService;

    public CatalogCategoryController(CategoryApplicationService categoryApplicationService) {
        this.categoryApplicationService = categoryApplicationService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<CategoryDto>>> getCategories(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "sort", required = false) String sort,
            @RequestParam(name = "status", required = false) CategoryStatus status,
            @RequestParam(name = "search", required = false) String search
    ) {
        PageResponse<CategoryDto> categories = categoryApplicationService.getCategories(page, size, sort, status, search);
        return ResponseEntity.ok(ApiResponse.success(categories));
    }

    @GetMapping("/{categoryId}")
    public ResponseEntity<ApiResponse<CategoryDto>> getCategoryByIdOrSlug(@PathVariable("categoryId") String categoryId) {
        CategoryDto category;
        try {
            UUID id = UUID.fromString(categoryId);
            category = categoryApplicationService.getCategoryById(id);
        } catch (IllegalArgumentException ex) {
            // Not a UUID, attempt lookup by slug
            category = categoryApplicationService.getCategoryBySlug(categoryId);
        }
        return ResponseEntity.ok(ApiResponse.success(category));
    }
}
