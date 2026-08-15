# ADR 0008: Shipping, Fulfilment & Delivery Orchestration

## Context
SPOREKART v3.0 requires a production-grade logistics and fulfilment subsystem capable of handling order dispatch, multi-carrier booking, AWB generation, real-time shipment tracking, delivery webhooks, and status synchronization with the Order domain (`Sprint 3F`).

The solution must support both internal/manual fulfilment and third-party logistics partners (such as Shiprocket, Delhivery, Bluedart) without coupling domain logic to any specific provider API.

## Decision
1. **Domain Boundary & Separation of Concerns**:
   - `Shipment` aggregate created in `com.sporekart.modules.shipment` with its own state machine (`ShipmentStateMachine`), tracking history, and package details.
   - `Order` domain retains strict ownership of commercial status (`READY_FOR_FULFILMENT`, `SHIPPED`, `OUT_FOR_DELIVERY`, `DELIVERED`).
   - `ShipmentApplicationService` propagates lifecycle status changes back to `OrderApplicationService`.

2. **Provider Abstraction (`ShippingProvider`)**:
   - Defined `ShippingProvider` interface acting as the abstraction port.
   - Implemented `MockShippingProvider` for deterministic dev/test execution.
   - Implemented `ShiprocketShippingProvider` for production Shiprocket API integration (with token caching and HMAC-SHA256 signature verification).
   - Created `ShippingProviderRegistry` factory for dynamic runtime resolution.

3. **Event-Driven Auto-Creation**:
   - `OrderLifecycleEventListener` listens for `OrderLifecycleEvent` with `newStatus == READY_FOR_FULFILMENT` to trigger asynchronous shipment creation and booking.

4. **Idempotency & Concurrency**:
   - Enforced database constraints `uq_shipment_provider_event` on `shipment_tracking_events` and `uq_shipping_provider_webhook` on `shipping_webhook_events`.
   - Prevented state regressions (e.g. `IN_TRANSIT` webhook arriving after `DELIVERED`).

5. **Reconciliation**:
   - Implemented `ShipmentReconciliationScheduler` running every 15 minutes to poll active shipments and sync statuses.

## Consequences
- Clean separation between commercial order state and logistics shipment state.
- Adding future logistics partners (e.g., Delhivery, Pickrr) requires only implementing `ShippingProvider`.
- Test suite executes deterministically with 100% pass rate using `MockShippingProvider`.
