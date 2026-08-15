# SPOREKART v3.0 — SPRINT 4H RECONNAISSANCE & ARCHITECTURE ANALYSIS

---

## 1. Executive Summary

Sprint 4H establishes a production-grade **Customer Service & Post-Purchase Operations domain** (`com.sporekart.modules.support`).
This document details our reconnaissance of the Sporekart codebase across Sprint 0 through Sprint 4G, outlining the canonical support ticket aggregate (`SupportTicket`), ticket lifecycle state machine (`SupportStateMachine`), conversation engine (`SupportMessage`), internal notes protection, SLA tracking, replacement workflow (`ReplacementRequest`), domain boundaries with Order, Payment, Shipping, Returns, and Inventory, Flyway migration plan (`V14`), security authorization rules, and REST endpoints.

---

## 2. Codebase Reconnaissance & Subsystem Audits

### 2.1 Domain Boundaries & Interplays
- **Support Domain (`com.sporekart.modules.support`)**: Coordinates post-purchase customer issue tickets (`TKT-2026-000001`), conversation messages, agent assignment, priority, SLA compliance, escalation, replacement request workflows (`RPL-2026-000001`), and cross-domain operational tracking.
- **Order Domain (`com.sporekart.modules.order`)**: Authoritative for order lifecycle state and item snapshots. Support reads order context without duplicating order data.
- **Payment & Refund Domain (`com.sporekart.modules.payment`)**: Authoritative for payment transactions and refund execution (`RefundRecordEntity`). Support displays refund status and invokes existing refund retry boundaries.
- **Shipping Domain (`com.sporekart.modules.shipment`)**: Authoritative for carrier transport, AWB booking, and tracking checkpoints. Support reads tracking timelines and requests replacement shipments.
- **Inventory Domain (`com.sporekart.modules.inventory`)**: Authoritative for stock availability, reservations, and stock movements. Support checks stock availability and requests stock reservations for replacement orders.
- **Returns Domain (`com.sporekart.modules.returns`)**: Authoritative for return eligibility, quality inspection, and return state transitions.

### 2.2 Database Schema Plan (`V14__support_disputes_replacements_domain.sql`)
Sprint 4H introduces four database tables:
1. `support_tickets`: `id`, `ticket_number`, `customer_id`, `order_id`, `order_reference`, `category`, `issue_type`, `status`, `priority`, `source`, `subject`, `description`, `assigned_agent_id`, `first_response_at`, `first_response_due_at`, `resolution_due_at`, `sla_status`, `resolved_at`, `closed_at`, `created_at`, `updated_at`, `version`.
2. `support_messages`: `id`, `ticket_id`, `author_id`, `author_type`, `visibility`, `content`, `attachment_urls`, `created_at`.
3. `replacement_requests`: `id`, `replacement_reference`, `ticket_id`, `ticket_number`, `order_id`, `order_reference`, `customer_id`, `order_item_id`, `product_id`, `sku`, `quantity`, `reason`, `status`, `reservation_id`, `replacement_shipment_id`, `admin_notes`, `approved_at`, `created_at`, `updated_at`.
4. `support_ticket_status_history`: `id`, `ticket_id`, `previous_status`, `new_status`, `reason`, `actor_type`, `actor_id`, `created_at`.

---

## 3. Support & Replacement Architecture Diagram

```
                             CUSTOMER
                                |
                                v
                          SUPPORT DOMAIN
                 (SupportTicket Aggregate Root)
                                |
      +-------------------------+-------------------------+
      |                         |                         |
      v                         v                         v
CONVERSATION & SLA      REPLACEMENT WORKFLOW       OPERATIONAL ACTIONS
(Public Messages &    (Inventory Check ->       (Order/Shipping/Return/
 Internal Notes)       Stock Reservation ->       Refund Cross-Domain
                       Replacement Shipment)        Read/Retry Hooks)
```

---

## 4. Safety & Security Invariants

1. **Internal Notes Privacy**: Messages with `visibility = INTERNAL_NOTE` are strictly filtered out of customer-facing DTO responses.
2. **Customer Ownership**: Customers can only view, create, or update tickets where `customerId` matches their authenticated identity.
3. **Idempotency & Concurrency**: Uniqueness constraints on `ticket_number` and `replacement_reference`; `@Version` optimistic locking on `SupportTicket`.
