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
    public ProductSearchCriteria {
        if (search != null) {
            search = search.trim();
            if (search.isEmpty()) {
                search = null;
            }
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
