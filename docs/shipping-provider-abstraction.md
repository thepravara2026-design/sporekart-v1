# SPOREKART v3.0 — CARRIER PROVIDER ABSTRACTION SPECIFICATION

---

## 1. Provider SPI Contract

The `ShippingProvider` interface defines normalized capabilities:
- `getProviderType()`: Identifies provider (`SHIPROCKET`, `DELHIVERY`, `BLUEDART`, `MOCK`).
- `createAndBookShipment(ShipmentBookingRequest)`: Books shipment with carrier and assigns AWB & tracking number.
- `cancelShipment(ShipmentCancellationRequest)`: Cancels active shipment booking with provider.
- `getTrackingInfo(providerShipmentId, awb)`: Fetches tracking checkpoints.
- `verifyWebhookSignature(rawBody, headers)`: Verifies provider webhook authenticity.
- `parseWebhookEvent(rawBody)`: Parses raw webhook JSON into normalized `NormalizedWebhookEvent`.
- `getLabelUrl(providerShipmentId, awb)`: Returns downloadable label URL.
- `getManifestUrl(providerShipmentId, awb)`: Returns downloadable manifest URL.

---

## 2. Dynamic Provider Registry

`ShippingProviderRegistry` dynamically resolves provider adapters by `ShipmentProviderType` enum. New providers (Delhivery, BlueDart, DTDC) can be added by implementing `ShippingProvider` without mutating core domain logic.
