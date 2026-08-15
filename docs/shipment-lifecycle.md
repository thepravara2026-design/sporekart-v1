# SPOREKART v3.0 — SHIPMENT LIFECYCLE & STATE MACHINE SPECIFICATION

---

## 1. Overview

The Shipment Lifecycle governs the end-to-end fulfilment of an order once payment is confirmed and inventory is reserved. The state machine guarantees deterministic status transitions, prevents state regressions, rejects invalid state jumps, and ensures append-only audit history logging.

---

## 2. Canonical Shipment State Transition Matrix

| Current Status | Allowed Target Statuses | Actor / Source | Transition Trigger / Event |
| :--- | :--- | :--- | :--- |
| `CREATED` | `READY_FOR_BOOKING`, `CANCELLED` | SYSTEM / ADMIN | Order reaches `READY_FOR_FULFILMENT` state |
| `READY_FOR_BOOKING` | `BOOKING_PENDING`, `BOOKED`, `CANCELLED` | SYSTEM / ADMIN | Booking engine initiates provider API request |
| `BOOKING_PENDING` | `BOOKED`, `EXCEPTION`, `CANCELLED` | SYSTEM / PROVIDER | Provider returns AWB and shipment details |
| `BOOKED` | `PICKUP_SCHEDULED`, `PICKED_UP`, `IN_TRANSIT`, `OUT_FOR_DELIVERY`, `DELIVERED`, `CANCELLED`, `EXCEPTION` | PROVIDER / WEBHOOK | Pickup scheduled / dispatched by courier |
| `PICKUP_SCHEDULED` | `PICKED_UP`, `IN_TRANSIT`, `OUT_FOR_DELIVERY`, `DELIVERED`, `CANCELLED`, `EXCEPTION` | PROVIDER / WEBHOOK | Courier picks up package from warehouse |
| `PICKED_UP` | `IN_TRANSIT`, `OUT_FOR_DELIVERY`, `DELIVERED`, `EXCEPTION` | PROVIDER / WEBHOOK | Package arrives at logistics hub |
| `IN_TRANSIT` | `OUT_FOR_DELIVERY`, `DELIVERED`, `DELIVERY_FAILED`, `RTO_INITIATED`, `EXCEPTION` | PROVIDER / WEBHOOK | Package moves between transit facilities |
| `OUT_FOR_DELIVERY` | `DELIVERED`, `DELIVERY_FAILED`, `RTO_INITIATED`, `EXCEPTION` | PROVIDER / WEBHOOK | Courier out for final delivery attempt |
| `DELIVERY_FAILED` | `OUT_FOR_DELIVERY`, `IN_TRANSIT`, `RTO_INITIATED`, `EXCEPTION` | PROVIDER / WEBHOOK | Failed delivery attempt / re-attempt scheduled |
| `RTO_INITIATED` | `RTO_IN_TRANSIT`, `RTO_DELIVERED`, `EXCEPTION` | PROVIDER / WEBHOOK | Return To Origin initiated by courier |
| `RTO_IN_TRANSIT` | `RTO_DELIVERED`, `EXCEPTION` | PROVIDER / WEBHOOK | Package returning to seller warehouse |
| `EXCEPTION` | `BOOKING_PENDING`, `BOOKED`, `IN_TRANSIT`, `OUT_FOR_DELIVERY`, `DELIVERED`, `CANCELLED` | ADMIN / RECONCILIATION | Operational override / reconciliation recovery |
| **`DELIVERED`** | *None (Terminal State)* | N/A | Package successfully handed to recipient |
| **`CANCELLED`** | *None (Terminal State)* | N/A | Shipment cancelled prior to pickup |
| **`RTO_DELIVERED`** | *None (Terminal State)* | N/A | Package successfully returned to origin |

---

## 3. Invariants & Rules

1. **Terminal Invariability**:
   - `DELIVERED`, `CANCELLED`, and `RTO_DELIVERED` are immutable terminal states. Any attempt to transition from a terminal state throws `InvalidShipmentStateTransitionException`.
2. **State Regression Prevention**:
   - Out-of-order webhooks (e.g. an `IN_TRANSIT` status payload arriving *after* a shipment is already marked `OUT_FOR_DELIVERY` or `DELIVERED`) are discarded as stale without regressing the shipment's aggregate status.
3. **Idempotency**:
   - Re-applying the current status returns the current `ShipmentDto` without creating duplicate status history records.
4. **Order Lifecycle Synchronization**:
   - Shipment status transitions automatically drive Order lifecycle transitions via `OrderApplicationService`:
     - `BOOKED` / `PICKED_UP` / `IN_TRANSIT` -> `Order.markShipped()`
     - `OUT_FOR_DELIVERY` -> `Order.markOutForDelivery()`
     - `DELIVERED` -> `Order.markDelivered()`
