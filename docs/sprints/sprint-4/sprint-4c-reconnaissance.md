# SPOREKART v3.0 — SPRINT 4C RECONNAISSANCE DOCUMENT
## SHIPPING ORCHESTRATION, SHIPROCKET INTEGRATION & PROVIDER-AGNOSTIC DELIVERY

---

### 1. Architectural Baseline & Existing Reconnaissance

A comprehensive analysis of the Sporekart v3.0 repository was conducted for Sprint 4C logistics engineering.

#### Key Findings from Codebase Audit:
1. **Existing Shipping Domain Structure (`com.sporekart.modules.shipment`)**:
   - **Domain Aggregate**: `Shipment.java` with package details, shipping address snapshot, tracking items, provider reference, AWB, tracking reference, and status machine.
   - **State Machine (`ShipmentStateMachine.java`)**:
     - Statuses: `CREATED`, `READY_FOR_BOOKING`, `BOOKING_PENDING`, `BOOKED`, `PICKUP_SCHEDULED`, `PICKED_UP`, `IN_TRANSIT`, `OUT_FOR_DELIVERY`, `DELIVERED`, `DELIVERY_FAILED`, `CANCELLED`, `RTO_INITIATED`, `RTO_IN_TRANSIT`, `RTO_DELIVERED`, `EXCEPTION`.
     - Terminal States: `DELIVERED`, `CANCELLED`, `RTO_DELIVERED`.
   - **Provider Boundary (`ShippingProvider.java`)**:
     - SPI Port: `getProviderType()`, `createAndBookShipment()`, `cancelShipment()`, `getTrackingInfo()`, `verifyWebhookSignature()`, `parseWebhookEvent()`.
     - Implementations: `MockShippingProvider.java`, `ShiprocketShippingProvider.java`.
     - Dispatcher: `ShippingProviderRegistry.java`.
   - **Application & Event Integration**:
     - `OrderLifecycleEventListener.java`: Listens for `OrderLifecycleEvent` when `newStatus == READY_FOR_FULFILMENT` to trigger shipment creation and booking.
     - `ShipmentApplicationService.java`: Orchestrates shipment creation, booking, customer tracking lookups, admin list/retry/cancel actions, webhook ingestion, and order status updates (`syncOrderStatus`).
     - `ShipmentReconciliationScheduler.java`: Periodically reconciles active shipments via provider APIs to handle missed/delayed webhooks.

2. **Existing Database Schema (`V9__shipping_domain.sql` & `V11`)**:
   - `shipments`: Core aggregate table with foreign key to `order_id`, tracking details, AWB, provider, and address snapshot.
   - `shipment_items`: Line item snapshot table.
   - `shipment_status_history`: Immutable status history audit table.
   - `shipment_tracking_events`: Tracking checkpoint history table with unique constraint `(shipment_id, provider_event_id)`.
   - `shipping_webhook_events`: Provider webhook idempotency log table with unique constraint `(provider, provider_event_id)`.

3. **Legacy Shipping Logic Analysis**:
   - Search across `order`, `payment`, `checkout`, `catalog` revealed **no competing manual shipping logic**.
   - `Shipment` aggregate is established as the **ONE canonical shipping system**.
   - `Order` domain does NOT maintain duplicate shipping status or tracking columns — it relies on `OrderLifecycle` status (`READY_FOR_FULFILMENT` -> `SHIPPED` -> `OUT_FOR_DELIVERY` -> `DELIVERED`).

---

### 2. Gaps & Refinement Plan for Sprint 4C

While the foundation exists, Sprint 4C requires hardening the architecture to FAANG-grade standards:

1. **Shiprocket Authentication & Production Adapter Hardening**:
   - `ShiprocketShippingProvider` must feature robust token management with expiring JWT token caching, auto-refresh upon expiry/401, thread-safe sync, and failure handling.
   - Add explicit configuration properties (`sporekart.shipping.provider=MOCK|SHIPROCKET`, `sporekart.shipping.shiprocket.api-url`, `sporekart.shipping.shiprocket.email`, `sporekart.shipping.shiprocket.password`, `sporekart.shipping.shiprocket.webhook-token`).

2. **Out-of-Order Webhook Handling & State Regression Prevention**:
   - Ensure webhook processing uses `ShipmentStateMachine.isStateRegression(current, incoming)` to discard/record stale webhooks (e.g. `IN_TRANSIT` arriving after `OUT_FOR_DELIVERY`) without regressing current state.

3. **Idempotency & Concurrency Hardening**:
   - Guarantee that concurrent shipment creation calls (`Admin A` and `Admin B` or network retry) yield exactly ONE shipment per order.
   - Ensure duplicate webhooks with the same `provider_event_id` return clean idempotent acknowledgments without appending duplicate history.

4. **Reconciliation Engine Refinement**:
   - Harden `ShipmentReconciliationService` / `reconcileActiveShipments` to catch state drifts between provider and local aggregate safely.

---

### 3. Proposed Final Architecture

```text
                           ORDER MODULE
                                │
                                ↓
                     OrderLifecycleEvent
                     (READY_FOR_FULFILMENT)
                                │
                                ↓
                   OrderLifecycleEventListener
                                │
                                ↓
                   ShipmentApplicationService
                                │
                                ↓
                   Shipment Aggregate (DB)
                                │
                      ShippingProviderRegistry
                                │
                ┌───────────────┴───────────────┐
                ↓                               ↓
       MockShippingProvider          ShiprocketShippingProvider
                │                               │
       (Dev / Test Sandbox)             (Shiprocket API v2)
                                                │
                                    ┌───────────┴───────────┐
                                    ↓                       ↓
                               API Call                 Webhook
                                    │                       │
                                    └───────────┬───────────┘
                                                ↓
                                    Webhook Verification
                                                │
                                                ↓
                                     Shipment State Machine
                                                │
                                                ↓
                                      Shipment Audit History
                                                │
                                                ↓
                                       Order Lifecycle Command
                                     (markShipped / markDelivered)
```
