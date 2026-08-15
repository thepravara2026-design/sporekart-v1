# SPOREKART v3.0 — SHIPPING FAILURE RECOVERY MATRIX

---

## Failure Recovery Specifications

| Scenario / Failure Event | Detection Mechanism | System Behavior | Recovery / Admin Action |
| :--- | :--- | :--- | :--- |
| **Shipment Booking Timeout** | External HTTP timeout | Shipment status set to `BOOKING_PENDING` / `CREATION_FAILED`. | Admin retry via `POST /api/v1/admin/shipments/{ref}/retry`. |
| **Invalid Webhook Signature** | Signature validation check | Returns 401 Unauthorized; rejects event. | Provider payload rejected safely. |
| **Duplicate Webhook Event** | `existsWebhookEvent` / DB UNIQUE index | Returns 200 OK without re-applying status transition. | Safe idempotent response. |
| **Out-of-Order Tracking Event** | Event timestamp ordering | Checkpoint recorded in ledger; state machine ignores regressive status jumps. | Ledger preserved; state machine protected. |
| **Delivery Failure / RTO** | Provider status `UNDELIVERED` / `RTO` | Shipment status updated to `DELIVERY_FAILED` / `RTO_INITIATED`. | Customer notified; reverse logistics initialized. |
| **Carrier Mismatch Mismatch** | `reconcileActiveShipments()` sync | Polled checkpoints synchronize local shipment status. | Automatic reconciliation sync. |
