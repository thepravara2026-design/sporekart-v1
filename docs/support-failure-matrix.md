# SPOREKART v3.0 — SUPPORT & REPLACEMENT FAILURE RECOVERY MATRIX

---

## Failure Recovery Specifications

| Failure Event | Detection Mechanism | System Behavior | Recovery / Admin Action |
| :--- | :--- | :--- | :--- |
| **Unauthorized Ticket Access** | `customerId` ownership check | Throws `AccessDeniedException` (HTTP 403). | Access blocked; security audit logged. |
| **Internal Note Exposure Risk** | DTO mapping layer filter | Strips messages with `visibility = INTERNAL_NOTE`. | Guaranteed customer privacy. |
| **Replacement Out of Stock** | Inventory availability check | `ReplacementRequest` set to `FAILED` / `REJECTED`. | Agent suggests refund or backorder. |
| **Concurrent Replacement Approval** | Optimistic locking (`@Version`) / unique key | Throws `OptimisticLockingFailureException`. | Transaction aborted; retry enforced. |
| **Replacement Shipment Creation Failure** | Shipping provider exception | `ReplacementRequest` remains `INVENTORY_RESERVED`. | Admin retries shipment booking via Support API. |
| **SLA Breach Detection** | Server-side `checkSlaBreaches()` | Sets `slaStatus = BREACHED`; transitions to `ESCALATED`. | Assigned agent notified for immediate resolution. |
