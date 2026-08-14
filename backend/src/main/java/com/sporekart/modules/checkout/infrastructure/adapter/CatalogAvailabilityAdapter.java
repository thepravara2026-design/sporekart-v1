package com.sporekart.modules.checkout.infrastructure.adapter;

import com.sporekart.modules.cart.domain.port.CatalogPort;
import com.sporekart.modules.checkout.domain.port.AvailabilityPort;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.UUID;

@Component
public class CatalogAvailabilityAdapter implements AvailabilityPort {

    private final CatalogPort catalogPort;

    public CatalogAvailabilityAdapter(CatalogPort catalogPort) {
        this.catalogPort = Objects.requireNonNull(catalogPort, "Catalog port cannot be null");
    }

    @Override
    public CatalogItemDetails getAuthoritativeItemDetails(UUID productId) {
        Objects.requireNonNull(productId, "Product ID cannot be null");
        CatalogPort.CatalogProductDetails details = catalogPort.getProductForCart(productId);
        return new CatalogItemDetails(
                details.id(),
                details.name(),
                details.sku(),
                details.price(),
                details.currency(),
                details.purchasable(),
                details.status()
        );
    }
}
