# SPOREKART v3.0 — SPRINT 3G: SHIPPING, FULFILMENT & DELIVERY ORCHESTRATION

## Architecture Overview

Sprint 3G establishes the provider-independent Shipping, Fulfilment & Delivery Subsystem for SPOREKART v3.0. It bridges Order creation (`Sprint 3C`), Payment (`Sprint 3E`), and Order State Machine (`Sprint 3F`) with physical dispatch, logistics partner integration, tracking timeline management, and automated order status synchronization.

```
                  +--------------------------+
                  |  Order Lifecycle Event   |
                  | (READY_FOR_FULFILMENT)   |
                  +------------+-------------+
                               |
                               v
                  +--------------------------+
                  | OrderLifecycleEventListener|
                  +------------+-------------+
                               |
                               v
                  +--------------------------+
                  | ShipmentApplicationService|
                  +------------+-------------+
                               |
            +------------------+------------------+
            |                                     |
            v                                     v
+-----------------------+             +-----------------------+
| MockShippingProvider  |             |ShiprocketShippingProv |
|  (Development/QAT)    |             |  (Production Adapter) |
+-----------------------+             +-----------------------+
```

## Key Architectural Principles

1. **Provider-Neutral Abstraction (`ShippingProvider`)**:
   - High-level business actions (`createAndBookShipment`, `trackShipment`, `cancelShipment`, `verifyWebhookSignature`, `parseWebhookEvent`) are exposed behind a clean SPI port.
   - Business services never depend directly on third-party APIs like `Shiprocket` or `Delhivery`.

2. **Order Lifecycle Ownership Isolation**:
   - `Order` domain retains complete ownership of Order status (`READY_FOR_FULFILMENT` -> `SHIPPED` -> `OUT_FOR_DELIVERY` -> `DELIVERED`).
   - `Shipment` domain owns Shipment status (`CREATED` -> `BOOKED` -> `IN_TRANSIT` -> `OUT_FOR_DELIVERY` -> `DELIVERED`).
   - `ShipmentApplicationService` delegates status updates back to `OrderApplicationService`.

3. **100% Webhook Idempotency & Replay Resilience**:
   - `shipping_webhook_events` table tracks raw webhooks with unique constraint `(provider, provider_event_id)`.
   - `shipment_tracking_events` enforces unique constraint `(shipment_id, provider_event_id)` to eliminate duplicate timeline entries.
   - Idempotent state transitions prevent out-of-order event regressions (e.g. `IN_TRANSIT` arriving after `DELIVERED`).

4. **Automated Reconciliation**:
   - Scheduled `ShipmentReconciliationScheduler` periodically queries active shipments (`BOOKED`, `PICKED_UP`, `IN_TRANSIT`, `OUT_FOR_DELIVERY`) via provider APIs to sync delivery statuses in case webhooks are dropped or delayed.

## API Endpoints

### Customer APIs
- `GET /api/v1/orders/{orderReference}/shipment`: View shipment details, courier information, AWB, and current status. (IDOR protected via `X-Customer-Id` validation).
- `GET /api/v1/orders/{orderReference}/tracking`: Detailed tracking timeline with timestamps, locations, and status events.

### Admin APIs
- `GET /api/v1/admin/shipments`: Paginated shipment management view with optional status filtering.
- `POST /api/v1/admin/shipments/{shipmentReference}/retry`: Manual retry for failed booking attempts.
- `POST /api/v1/admin/shipments/{shipmentReference}/cancel`: Admin cancellation of active shipments.
- `POST /api/v1/admin/shipments/reconcile`: Trigger manual background reconciliation run.

### Webhook API
- `POST /api/v1/webhooks/shipping/{provider}`: Standardized webhook receiver supporting dynamic provider dispatch (`mock`, `shiprocket`) with signature verification.
