package com.sporekart.modules.catalog.controller;

import com.sporekart.application.exception.ApiResponse;
import com.sporekart.modules.catalog.application.ProductApplicationService;
import com.sporekart.modules.catalog.application.ProductDto;
import com.sporekart.modules.catalog.application.dto.response.PageResponse;
import com.sporekart.modules.catalog.domain.product.ProductStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@Tag(name = "Catalog Products", description = "Public REST endpoints for product catalog search, filtering, and retrieval")
@RestController
@RequestMapping("/api/v1/catalog/products")
public class CatalogProductController {

    private final ProductApplicationService productApplicationService;

    public CatalogProductController(ProductApplicationService productApplicationService) {
        this.productApplicationService = productApplicationService;
    }

    @Operation(
            summary = "Get Paginated Product Catalog",
            description = "Retrieves a paginated list of catalog products supporting optional text search, category filtering, product status filtering, price range bounds, and sorting."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Paginated products retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request parameter (e.g. invalid page size, sort field, or negative price range)")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ProductDto>>> getProducts(
            @Parameter(description = "Zero-indexed page number", example = "0")
            @RequestParam(name = "page", defaultValue = "0") int page,

            @Parameter(description = "Number of items per page (max 100)", example = "20")
            @RequestParam(name = "size", defaultValue = "20") int size,

            @Parameter(description = "Sort field and direction (e.g., name,asc or price,desc)", example = "createdAt,desc")
            @RequestParam(name = "sort", required = false) String sort,

            @Parameter(description = "Category UUID filter", example = "c1f71f65-3850-482a-921c-4e899b80b72a")
            @RequestParam(name = "categoryId", required = false) UUID categoryId,

            @Parameter(description = "Product status filter", example = "ACTIVE")
            @RequestParam(name = "status", required = false) ProductStatus status,

            @Parameter(description = "Search term for product name, SKU, or description", example = "oyster")
            @RequestParam(name = "search", required = false) String search,

            @Parameter(description = "Minimum price bound", example = "10.00")
            @RequestParam(name = "minPrice", required = false) BigDecimal minPrice,

            @Parameter(description = "Maximum price bound", example = "50.00")
            @RequestParam(name = "maxPrice", required = false) BigDecimal maxPrice
    ) {
        PageResponse<ProductDto> products = productApplicationService.getProducts(page, size, sort, categoryId, status, search, minPrice, maxPrice);
        return ResponseEntity.ok(ApiResponse.success(products));
    }

    @Operation(
            summary = "Get Product Detail by ID or SKU",
            description = "Retrieves details for a single product by UUID identifier or SKU code."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Product details retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Product not found with specified ID or SKU")
    })
    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductDto>> getProductByIdOrSku(
            @Parameter(description = "Product UUID or unique SKU code", example = "SKU-LION-001")
            @PathVariable("productId") String productId
    ) {
        ProductDto product;
        try {
            UUID id = UUID.fromString(productId);
            product = productApplicationService.getProductById(id);
        } catch (IllegalArgumentException ex) {
            // Not a UUID, attempt lookup by SKU
            product = productApplicationService.getProductBySku(productId);
        }
        return ResponseEntity.ok(ApiResponse.success(product));
    }
}
