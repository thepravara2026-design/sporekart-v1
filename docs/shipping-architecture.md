# SPOREKART v3.0 — SHIPPING ARCHITECTURE & PROVIDER ABSTRACTION

---

## 1. Core Architectural Strategy

Sporekart v3.0 enforces a strict provider-agnostic shipping boundary. Third-party logistics SDKs or provider-specific REST APIs (e.g. Shiprocket, Delhivery, Bluedart) must **never** leak into the core `Order` or `Shipment` domain aggregates.

---

## 2. SPI Boundary Interfaces

```java
public interface ShippingProvider {
    ShipmentProviderType getProviderType();
    ShipmentBookingResult createAndBookShipment(ShipmentBookingRequest request);
    ShipmentCancellationResult cancelShipment(ShipmentCancellationRequest request);
    ShipmentTrackingResult getTrackingInfo(String providerShipmentId, String awb);
    boolean verifyWebhookSignature(String rawBody, Map<String, String> headers);
    NormalizedWebhookEvent parseWebhookEvent(String rawBody);
}
```

### Provider Registry & Selection

- `ShippingProviderRegistry`: Manages all active `ShippingProvider` implementations (`MockShippingProvider`, `ShiprocketShippingProvider`).
- Provider resolution is driven by configuration property `sporekart.shipping.provider` (`MOCK` vs `SHIPROCKET`).

---

## 3. Data Transfer Objects (DTOs)

- `ShipmentBookingRequest`: Contains shipment reference, order reference, shipping address snapshot, package details, and item snapshots.
- `ShipmentBookingResult`: Encapsulates provider shipment ID, AWB, tracking number, courier name, courier code, and estimated delivery date.
- `NormalizedWebhookEvent`: Standardized provider event containing `providerEventId`, `providerShipmentId`, `awb`, `orderReference`, `rawProviderStatus`, `normalizedStatus` (`ShipmentStatus`), `description`, `location`, and `occurredAt`.
