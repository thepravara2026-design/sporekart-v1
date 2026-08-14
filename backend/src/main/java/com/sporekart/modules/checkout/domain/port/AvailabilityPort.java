package com.sporekart.modules.checkout.domain.port;

import java.math.BigDecimal;
import java.util.UUID;

public interface AvailabilityPort {

    CatalogItemDetails getAuthoritativeItemDetails(UUID productId);

    record CatalogItemDetails(
            UUID productId,
            String name,
            String sku,
            BigDecimal price,
            String currency,
            boolean purchasable,
            String status
    ) {}
}
