# SPOREKART v3.0 — RETURNS & REFUNDS FAILURE RECOVERY MATRIX

---

## Failure Recovery Specifications

| Failure Event | Detection Mechanism | System Behavior | Recovery / Admin Action |
| :--- | :--- | :--- | :--- |
| **Duplicate Return Request** | Quantity ceiling check | Throws `ReturnEligibilityException`. | Rejects request; prevents over-returning. |
| **Gateway Refund Timeout** | Provider exception | Refund record marked `FAILED` / `PENDING`; return set to `EXCEPTION`. | Admin retry via `POST /api/v1/admin/returns/{ref}/refund/retry`. |
| **Duplicate Gateway Refund** | Idempotency key check (`idempotency_key`) | Returns existing `PROCESSED` refund record. | Prevents double refunding. |
| **Inspection Rejection** | Inspection outcome `REJECT` | Return set to `RETURN_REJECTED`. No refund or stock restock. | Admin notes captured; customer notified. |
| **Inventory Restock Failure** | `InventoryReturnEventListener` exception | Logged warning; restock retried via background event listener. | Idempotent restock processing. |
