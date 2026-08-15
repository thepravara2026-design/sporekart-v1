package com.sporekart.modules.shipment.domain;

import java.math.BigDecimal;

public interface CarrierSelectionPort {

    ShipmentProviderType selectProvider(
            String destinationPostalCode,
            PackageDetails packageDetails,
            BigDecimal orderTotal,
            boolean isCod
    );
}
