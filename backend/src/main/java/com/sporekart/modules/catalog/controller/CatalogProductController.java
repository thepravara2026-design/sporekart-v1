package com.sporekart.modules.catalog.controller;

import com.sporekart.application.exception.ApiResponse;
import com.sporekart.modules.catalog.application.ProductApplicationService;
import com.sporekart.modules.catalog.application.ProductDto;
import com.sporekart.modules.catalog.application.dto.response.PageResponse;
import com.sporekart.modules.catalog.domain.product.ProductStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/catalog/products")
public class CatalogProductController {

    private final ProductApplicationService productApplicationService;

    public CatalogProductController(ProductApplicationService productApplicationService) {
        this.productApplicationService = productApplicationService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ProductDto>>> getProducts(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "sort", required = false) String sort,
            @RequestParam(name = "categoryId", required = false) UUID categoryId,
            @RequestParam(name = "status", required = false) ProductStatus status,
            @RequestParam(name = "search", required = false) String search
    ) {
        PageResponse<ProductDto> products = productApplicationService.getProducts(page, size, sort, categoryId, status, search);
        return ResponseEntity.ok(ApiResponse.success(products));
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductDto>> getProductByIdOrSku(@PathVariable("productId") String productId) {
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
