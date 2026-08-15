package com.sporekart.modules.shipment.infrastructure.provider;

import com.sporekart.modules.shipment.domain.ShipmentProviderType;
import com.sporekart.modules.shipment.infrastructure.provider.dto.NormalizedWebhookEvent;
import com.sporekart.modules.shipment.infrastructure.provider.dto.ShipmentBookingRequest;
import com.sporekart.modules.shipment.infrastructure.provider.dto.ShipmentBookingResult;
import com.sporekart.modules.shipment.infrastructure.provider.dto.ShipmentCancellationRequest;
import com.sporekart.modules.shipment.infrastructure.provider.dto.ShipmentCancellationResult;
import com.sporekart.modules.shipment.infrastructure.provider.dto.ShipmentTrackingResult;

import java.util.Map;

public interface ShippingProvider {
    ShipmentProviderType getProviderType();

    ShipmentBookingResult createAndBookShipment(ShipmentBookingRequest request);

    ShipmentCancellationResult cancelShipment(ShipmentCancellationRequest request);

    ShipmentTrackingResult getTrackingInfo(String providerShipmentId, String awb);

    boolean verifyWebhookSignature(String rawBody, Map<String, String> headers);

    NormalizedWebhookEvent parseWebhookEvent(String rawBody);
}
