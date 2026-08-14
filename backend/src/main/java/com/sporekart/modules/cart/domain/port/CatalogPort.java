package com.sporekart.modules.cart.domain.port;

import java.math.BigDecimal;
import java.util.UUID;

public interface CatalogPort {

    CatalogProductDetails getProductForCart(UUID productId);

    record CatalogProductDetails(
            UUID id,
            String name,
            String sku,
            BigDecimal price,
            String currency,
            boolean purchasable,
            String status
    ) {}
}
