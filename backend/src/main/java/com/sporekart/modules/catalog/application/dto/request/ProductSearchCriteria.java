package com.sporekart.modules.catalog.application.dto.request;

import com.sporekart.modules.catalog.domain.product.ProductStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductSearchCriteria(
        String search,
        UUID categoryId,
        ProductStatus status,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        int page,
        int size,
        String sort
) {
    private static final int MAX_SEARCH_LENGTH = 100;

    public ProductSearchCriteria {
        if (search != null) {
            search = search.trim();
            if (search.isEmpty()) {
                search = null;
            } else {
                if (search.length() > MAX_SEARCH_LENGTH) {
                    throw new IllegalArgumentException("Search query cannot exceed " + MAX_SEARCH_LENGTH + " characters");
                }
                String stripped = search.replaceAll("[%\\*\\?_]", "").trim();
                if (stripped.isEmpty()) {
                    throw new IllegalArgumentException("Search query contains excessive wildcards");
                }
            }
        }
        if (page < 0) {
            throw new IllegalArgumentException("Page index cannot be negative");
        }
        if (size <= 0 || size > 100) {
            throw new IllegalArgumentException("Page size cannot exceed maximum limit of 100");
        }
    }

    public static ProductSearchCriteria of(
            int page,
            int size,
            String sort,
            UUID categoryId,
            ProductStatus status,
            String search,
            BigDecimal minPrice,
            BigDecimal maxPrice
    ) {
        return new ProductSearchCriteria(search, categoryId, status, minPrice, maxPrice, page, size, sort);
    }
}
