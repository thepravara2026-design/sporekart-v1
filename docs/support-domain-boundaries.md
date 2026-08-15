# SPOREKART v3.0 — SUPPORT DOMAIN BOUNDARIES SPECIFICATION

---

## 1. Domain Ownership Matrix

| Domain | Authoritative Responsibilities |
| :--- | :--- |
| **Support** | Ticket lifecycle, conversation history, customer visibility filtering, agent assignment, priority, SLA tracking, escalation, replacement request aggregate. |
| **Order** | Order state machine, order items, original prices, order addresses. |
| **Payment / Refund** | Payment status, gateway interactions, refund execution (`RefundRecordEntity`). |
| **Shipping** | Waybill booking, carrier abstraction, shipment status, tracking events. |
| **Inventory** | Stock availability, on-hand ledger, reservations, stock adjustments. |
| **Returns** | Return requests, return eligibility, physical inspection, return acceptance. |

---

## 2. Anti-Patterns Strictly Avoided

- Support DOES NOT write directly to `orders`, `payments`, `refund_records`, `inventory_items`, `shipments`, or `returns` tables.
- Support DOES NOT mutate domain state without calling domain application service APIs.
- Support DOES NOT leak internal notes to customer API consumers.
