# SPOREKART v3.0 — REVERSE LOGISTICS SPECIFICATION

---

## 1. Shipping Domain Integration

Reverse logistics reuses Sprint 4F `ShipmentApplicationService` provider abstraction without creating duplicate shipping logic inside Returns.

Workflow:
1. `ReturnApplicationService.createReverseShipment(returnRef, adminId)` generates reverse shipment reference.
2. Invokes `ShipmentApplicationService.createShipmentForOrder(orderId)` or assigns reverse shipment ID.
3. Carrier pickup and tracking checkpoints update `returns` status (`PICKUP_SCHEDULED` -> `PICKED_UP` -> `IN_TRANSIT` -> `RECEIVED`).
