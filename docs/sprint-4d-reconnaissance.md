# SPOREKART v3.0 — SPRINT 4D RECONNAISSANCE & ARCHITECTURE ANALYSIS

---

## 1. Executive Summary

Sprint 4D focuses on **Returns, Refunds, Reverse Logistics & Admin Return Operations**.
This document synthesizes our reconnaissance of the existing Sporekart codebase across Sprint 0 through Sprint 4C, outlining the exact domain model, existing Flyway migrations, reverse logistics provider integration, payment refund orchestration, inventory restock boundaries, state transitions, idempotency mechanisms, and frontend/backend API contracts.

---

## 2. Codebase Reconnaissance & Legacy Findings

### 2.1 Existing Database Schema (Flyway V10)
Migration [`V10__returns_refunds_domain.sql`](file:///f:/sporekart-v3.0/backend/src/main/resources/db/migration/V10__returns_refunds_domain.sql) defines five core tables for the return subsystem:
- `returns`: `id`, `return_reference`, `order_id`, `order_reference`, `customer_id`, `status`, `reason_code`, `reason_description`, `evidence_urls`, `policy_version`, timestamps (`requested_at`, `approved_at`, `received_at`, `inspected_at`, `completed_at`), and `version`.
- `return_items`: `id`, `return_id`, `order_item_id`, `product_id`, `sku`, `product_name_snapshot`, `requested_quantity`, `approved_quantity`, `received_quantity`, `accepted_quantity`, `rejected_quantity`, `unit_price`, `refund_amount`, `reason_code`.
- `return_status_history`: `id`, `return_id`, `previous_status`, `new_status`, `reason`, `actor_type`, `actor_id`, `correlation_id`, `created_at`.
- `return_inspections`: `id`, `return_id`, `inspector_id`, `outcome`, `notes`, `inspected_at`.
- `refund_records`: `id`, `refund_reference`, `return_id`, `return_reference`, `order_id`, `customer_id`, `payment_id`, `payment_reference`, `provider`, `amount`, `currency`, `status`, `failure_reason`, `provider_refund_id`, `idempotency_key`, `version`.

### 2.2 Domain Aggregates & Boundaries
- **Order Domain (`com.sporekart.modules.order`)**: Authoritative for order state (`DELIVERED`, `SHIPPED`). Contains order line items (`OrderItem`) and customer ownership.
- **Shipment Domain (`com.sporekart.modules.shipment`)**: Authoritative for forward delivery and carrier integration (Sprint 4C). Reusable SPI boundary (`ShippingProvider`) for reverse shipment creation and carrier booking.
- **Payment Domain (`com.sporekart.modules.payment`)**: Authoritative for payment attempts and Razorpay / Mock provider refund calls (Sprint 4B). `PaymentProvider.processRefund(PaymentRefundRequest)` handles payment provider interaction.
- **Return Domain (`com.sporekart.modules.returns`)**: Authoritative for return request aggregate (`Return`), line item breakdown (`ReturnItem`), return state machine (`ReturnStateMachine`), return eligibility rules (`ReturnEligibilityService`), inspection outcome evaluation (`ReturnInspection`), and refund record creation (`RefundRecordEntity`).

---

## 3. Reverse Logistics & Provider Boundary (Sprint 4C Reuse)

The reverse shipment lifecycle reuses the `ShippingProvider` boundary established in Sprint 4C:
```
           +-----------------------------------------+
           |           Return Domain                 |
           +-----------------------------------------+
                                |
                                v
           +-----------------------------------------+
           |   ReverseShippingApplicationService     |
           +-----------------------------------------+
                                |
                                v
           +-----------------------------------------+
           |       ShippingProvider SPI              |
           +-----------------------------------------+
                                |
           +--------------------+--------------------+
           |                                         |
           v                                         v
+-----------------------+                 +---------------------+
|  Shiprocket Adapter   |                 | Mock Shipping Adapt |
+-----------------------+                 +---------------------+
```

---

## 4. Refund Idempotency & Payment Integration (Sprint 4B Reuse)

Refund execution invokes `PaymentProvider.processRefund(...)` using a deterministic idempotency key format: `RFD-{returnReference}`.
If a network timeout occurs or a duplicate refund request is issued, the system checks `refund_records` for existing `idempotency_key` and returns the recorded status cleanly without issuing duplicate payment provider charges.

---

## 5. Identified Architectural Enhancements for Sprint 4D

1. **Reverse Logistics Orchestration**:
   - Link `Return` to reverse shipment tracking via explicit `reverseShipmentId` or reverse shipment booking integration upon Return approval.
   - Listen to reverse shipment lifecycle events (`ShipmentLifecycleEvent`) to automatically advance Return status:
     - Reverse `PICKED_UP` -> Return `PICKED_UP`
     - Reverse `IN_TRANSIT` -> Return `IN_TRANSIT`
     - Reverse `DELIVERED` -> Return `RECEIVED` -> `INSPECTION_PENDING`
   - Support pickup failure handling (`PICKUP_FAILED`).

2. **Partial Returns & Quantity Limit Invariants**:
   - Enforce invariant: `requested_quantity + previously_returned_quantity <= purchased_quantity` for every `order_item_id`.
   - Prevent returning non-returnable categories or expired return window (`deliveredAt + 7 days`).

3. **Inspection & Inventory Restock Boundary**:
   - Upon inspection completion, accepted items emit `ReturnItemAcceptedInventoryEvent` or invoke inventory restock to increase available inventory or flag damaged inventory without violating inventory domain isolation.

4. **Refund Reconciliation**:
   - Add `reconcileRefundStatus(returnReference)` in `ReturnApplicationService` to poll provider refund status and converge local state if provider processed refund out-of-band.

5. **Customer & Admin Endpoints & UI**:
   - Customer endpoints: `GET /api/v1/orders/{orderRef}/returns/eligibility`, `POST /api/v1/orders/{orderRef}/returns`, `GET /api/v1/returns/{returnRef}`, `POST /api/v1/returns/{returnRef}/cancel`.
   - Admin endpoints: `GET /api/v1/admin/returns`, `GET /api/v1/admin/returns/{returnRef}`, `POST /api/v1/admin/returns/{returnRef}/approve`, `POST /api/v1/admin/returns/{returnRef}/reject`, `POST /api/v1/admin/returns/{returnRef}/create-reverse-shipment`, `POST /api/v1/admin/returns/{returnRef}/inspect`, `POST /api/v1/admin/returns/{returnRef}/refund`, `POST /api/v1/admin/returns/{returnRef}/reconcile`.

---

## 6. Migration & Safety Plan

- All database schema tables (`returns`, `return_items`, `return_status_history`, `return_inspections`, `refund_records`) are managed cleanly via Flyway `V10__returns_refunds_domain.sql`.
- No destructive alterations needed.
- Full test suite verification across backend (`mvn clean test`) and frontend (`npm run test -- --run` & `npm run build`).
