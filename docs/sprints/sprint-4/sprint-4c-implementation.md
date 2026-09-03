# SPOREKART v3.0 — SPRINT 4C IMPLEMENTATION DETAILS

---

### 1. Architectural Implementation Overview

Sprint 4C delivers a provider-agnostic logistics subsystem capable of handling shipment creation, provider allocation, AWB generation, real-time tracking, out-of-order webhook processing, reconciliation, and integration with the Sprint 4A Order Lifecycle.

---

### 2. Core Components Implemented / Hardened

1. **Shiprocket Provider & Auth Token Caching (`ShiprocketShippingProvider.java`)**:
   - Implemented `ShiprocketShippingProvider` SPI adapter implementing `ShippingProvider`.
   - Hardened `getAuthToken()` with thread-safe JWT caching and auto-refresh logic.
   - Centralized status mapping converts raw Shiprocket statuses (`DELIVERED`, `OUT FOR DELIVERY`, `PICKED UP`, `IN TRANSIT`, `RTO`, `UNDELIVERED`, `CANCELED`) into canonical `ShipmentStatus` enum values.
   - Enhanced `verifyWebhookSignature` to support `x-api-key` and `x-shiprocket-token` header validation.

2. **Shipment Lifecycle & State Machine (`ShipmentStateMachine.java` & `Shipment.java`)**:
   - Enforced transition invariants across 15 statuses (`CREATED` -> `READY_FOR_BOOKING` -> `BOOKING_PENDING` -> `BOOKED` -> `PICKUP_SCHEDULED` -> `PICKED_UP` -> `IN_TRANSIT` -> `OUT_FOR_DELIVERY` -> `DELIVERED`).
   - Hardened terminal state locks on `DELIVERED`, `CANCELLED`, and `RTO_DELIVERED`.
   - Prevented state regressions: Out-of-order webhooks (e.g. `IN_TRANSIT` arriving after `OUT_FOR_DELIVERY` or `DELIVERED`) are recorded as audit events without regressing current aggregate status.

3. **Webhook Processing & Idempotency (`ShipmentApplicationService.java`)**:
   - Database constraint `shipping_webhook_events(provider, provider_event_id)` and `shipment_tracking_events(shipment_id, provider_event_id)` prevent duplicate webhook processing.

4. **Order Domain Integration**:
   - Automatically converts shipment status changes into `OrderApplicationService` commands (`markShipped`, `markOutForDelivery`, `markDelivered`).
   - Order domain remains authoritative for order lifecycle while Shipment domain remains authoritative for shipment aggregate state.

5. **Automated & Admin Reconciliation (`ShipmentApplicationService.reconcileActiveShipments`)**:
   - Periodically queries active shipments (`BOOKED`, `PICKUP_SCHEDULED`, `PICKED_UP`, `IN_TRANSIT`, `OUT_FOR_DELIVERY`) via provider APIs to sync status in case of delayed or dropped webhooks.
