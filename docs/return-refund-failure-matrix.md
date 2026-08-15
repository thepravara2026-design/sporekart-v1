# SPOREKART v3.0 — RETURN & REFUND FAILURE RECOVERY MATRIX

---

## Failure Recovery Specifications

| Scenario / Failure Event | Detection Mechanism | System Behavior | Recovery / Admin Action |
| :--- | :--- | :--- | :--- |
| **Customer Double-Click Return** | DB Unique Index on return request / idempotency | Second request throws `ReturnEligibilityException` or returns existing return. | Safe idempotent response returned. |
| **Concurrent Returns for Same Line Item** | Database row lock / transactional quantity evaluation | Evaluates `requested + existing_returned > purchased`. Excess request rejected. | First transaction succeeds; second fails validation. |
| **Reverse Shipment Provider Timeout** | Provider call timeout / network exception | Return remains in `APPROVED` state with error log. | Admin clicks `Create Reverse Shipment` or retry process runs. |
| **Pickup Failed / Customer Unavailable** | Provider webhook `PICKUP_FAILED` | Return status set to `PICKUP_FAILED`. | Admin reschedules pickup or customer contacts support. |
| **Warehouse Inspection Rejection** | Inspector marks outcome `RETURN_REJECTED` | Return transitions to `RETURN_REJECTED` (terminal). No refund issued. | Customer notified with inspection notes & evidence. |
| **Payment Provider Refund Timeout** | Network timeout during `processRefund` | Refund record remains `PENDING` or `INITIATED`; Return set to `EXCEPTION`. | Admin clicks `Reconcile Refund` to poll provider status. |
| **Duplicate Refund Webhook** | `refund_records.idempotency_key` unique check | Webhook event recorded; aggregate transition skipped if already `PROCESSED`. | Idempotent HTTP 200 returned. |
| **Stale Webhook Event** | `ReturnStateMachine.validateTransition` | Transition ignored or recorded in audit history without regressing state. | Audit log recorded. |
