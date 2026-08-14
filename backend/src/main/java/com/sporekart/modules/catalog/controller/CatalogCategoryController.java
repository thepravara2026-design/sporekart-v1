package com.sporekart.modules.catalog.controller;

import com.sporekart.application.exception.ApiResponse;
import com.sporekart.modules.catalog.application.CategoryApplicationService;
import com.sporekart.modules.catalog.application.CategoryDto;
import com.sporekart.modules.catalog.application.dto.response.PageResponse;
import com.sporekart.modules.catalog.domain.category.CategoryStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Catalog Categories", description = "Public REST endpoints for category list browsing and retrieval")
@RestController
@RequestMapping("/api/v1/catalog/categories")
public class CatalogCategoryController {

    private final CategoryApplicationService categoryApplicationService;

    public CatalogCategoryController(CategoryApplicationService categoryApplicationService) {
        this.categoryApplicationService = categoryApplicationService;
    }

    @Operation(
            summary = "Get Paginated Categories",
            description = "Retrieves a paginated list of catalog categories with optional text search, status filtering, and sorting."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Paginated categories retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request parameter")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<CategoryDto>>> getCategories(
            @Parameter(description = "Zero-indexed page number", example = "0")
            @RequestParam(name = "page", defaultValue = "0") int page,

            @Parameter(description = "Number of items per page (max 100)", example = "20")
            @RequestParam(name = "size", defaultValue = "20") int size,

            @Parameter(description = "Sort field and direction (e.g., name,asc or slug,asc)", example = "name,asc")
            @RequestParam(name = "sort", required = false) String sort,

            @Parameter(description = "Category status filter", example = "ACTIVE")
            @RequestParam(name = "status", required = false) CategoryStatus status,

            @Parameter(description = "Search term for category name or description", example = "medicinal")
            @RequestParam(name = "search", required = false) String search
    ) {
        PageResponse<CategoryDto> categories = categoryApplicationService.getCategories(page, size, sort, status, search);
        return ResponseEntity.ok(ApiResponse.success(categories));
    }

    @Operation(
            summary = "Get Category Detail by ID or Slug",
            description = "Retrieves details for a single category by UUID identifier or slug string."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Category details retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Category not found with specified ID or slug")
    })
    @GetMapping("/{categoryId}")
    public ResponseEntity<ApiResponse<CategoryDto>> getCategoryByIdOrSlug(
            @Parameter(description = "Category UUID or URL slug", example = "medicinal-mushrooms")
            @PathVariable("categoryId") String categoryId
    ) {
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
