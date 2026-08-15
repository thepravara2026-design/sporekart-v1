# SPOREKART v3.0 — REVERSE LOGISTICS SPECIFICATION

---

## 1. Overview

Reverse logistics manages the pickup and transit of returned items from the customer back to the warehouse.
It integrates with Sprint 4C's `ShippingProvider` boundary (Shiprocket API v2 & Mock Provider).

---

## 2. Event-Driven Workflow

1. **Return Approval**: Admin approves Return -> `ReturnApprovedEvent` emitted -> `ReturnEventListener` creates reverse shipment via `ShipmentApplicationService`.
2. **Reverse Carrier Booking**: `ShipmentApplicationService` books pickup with `ShippingProvider` (Shiprocket / Mock).
3. **Carrier Status Sync**: `ShipmentStatusEventListener` listens to `ShipmentLifecycleEvent`:
   - `BOOKED` / `PICKUP_SCHEDULED` -> Return status set to `PICKUP_SCHEDULED`.
   - `PICKED_UP` -> Return status set to `PICKED_UP`.
   - `IN_TRANSIT` -> Return status set to `IN_TRANSIT`.
   - `DELIVERED` -> Return status set to `RECEIVED` -> `INSPECTION_PENDING`.
   - `DELIVERY_FAILED` -> Return status set to `PICKUP_FAILED`.
