package com.sporekart.modules.shipment.infrastructure.provider;

import com.sporekart.modules.shipment.domain.CarrierSelectionPort;
import com.sporekart.modules.shipment.domain.PackageDetails;
import com.sporekart.modules.shipment.domain.ShipmentProviderType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DefaultCarrierSelectionStrategy implements CarrierSelectionPort {

    @Value("${sporekart.shipping.default-provider:SHIPROCKET}")
    private String defaultProvider;

    @Override
    public ShipmentProviderType selectProvider(
            String destinationPostalCode,
            PackageDetails packageDetails,
            BigDecimal orderTotal,
            boolean isCod
    ) {
        if ("MOCK".equalsIgnoreCase(defaultProvider)) {
            return ShipmentProviderType.MOCK;
        }
        return ShipmentProviderType.SHIPROCKET;
    }
}
