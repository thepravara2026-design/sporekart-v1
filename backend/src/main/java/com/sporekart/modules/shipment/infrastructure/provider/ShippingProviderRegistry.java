package com.sporekart.modules.shipment.infrastructure.provider;

import com.sporekart.modules.shipment.domain.ShipmentProviderType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ShippingProviderRegistry {

    private final Map<ShipmentProviderType, ShippingProvider> providerMap;

    @Value("${sporekart.shipping.provider:MOCK}")
    private String defaultProviderName;

    public ShippingProviderRegistry(List<ShippingProvider> providers) {
        this.providerMap = providers.stream()
                .collect(Collectors.toMap(ShippingProvider::getProviderType, Function.identity()));
    }

    public ShippingProvider getProvider(ShipmentProviderType type) {
        ShippingProvider provider = providerMap.get(type);
        if (provider == null) {
            throw new IllegalArgumentException("Unsupported shipping provider type: " + type);
        }
        return provider;
    }

    public ShippingProvider getDefaultProvider() {
        ShipmentProviderType type = ShipmentProviderType.valueOf(defaultProviderName.toUpperCase());
        return getProvider(type);
    }
}
