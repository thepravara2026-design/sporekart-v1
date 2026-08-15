# SPOREKART v3.0 — SHIPPING RECONCILIATION SPECIFICATION

---

## 1. Reconciliation Engine

`ShipmentApplicationService.reconcileActiveShipments()` handles state mismatches between local shipment status and external provider status.

Features:
- **Batch Reconciliation**: Selects active non-terminal shipments (`BOOKED`, `PICKED_UP`, `IN_TRANSIT`, `OUT_FOR_DELIVERY`).
- **Provider Polling**: Calls `provider.getTrackingInfo(providerShipmentId, awb)`.
- **Deduplicated Appends**: Appends missing checkpoints to `shipment_tracking_events` and syncs order status.
- **Admin Endpoint**: `POST /api/v1/admin/shipments/{shipmentReference}/sync` allows manual single-shipment reconciliation.
