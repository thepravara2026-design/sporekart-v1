package com.sporekart.modules.shipment.infrastructure.provider.dto;

import com.sporekart.modules.shipment.domain.PackageDetails;
import com.sporekart.modules.shipment.domain.ShippingAddressSnapshot;

import java.util.List;

public record ShipmentBookingRequest(
        String shipmentReference,
        String orderReference,
        ShippingAddressSnapshot shippingAddress,
        PackageDetails packageDetails,
        List<BookingItem> items
) {
    public record BookingItem(
            String sku,
            String name,
            int quantity
    ) {}
}
