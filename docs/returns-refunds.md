# SPOREKART v3.0 — Returns, Reverse Logistics & Refund Orchestration Subsystem

## Overview
The Returns, Reverse Logistics & Refund Orchestration subsystem (Sprint 3H) provides production-grade, domain-driven management of customer return requests, warehouse inspection processing, inventory restoration, and idempotent refund execution.

---

## Key Domain Rules & Principles
1. **Strict Context Boundaries**:
   - **Order Domain**: Owns original financial transactions, order line items, and delivery status.
   - **Return Domain**: Owns return requests, 14-day eligibility calculations, policy snapshot rules, warehouse inspection outcomes, and refund orchestration.
   - **Shipping Domain**: Owns reverse logistics booking, AWB tracking, and carrier webhooks (reused from Sprint 3G).
   - **Payment Domain**: Owns actual refund execution (`PaymentProvider` SPI extension with `processRefund`), refund attempt records, and provider API communications.
   - **Inventory Domain**: Owns stock adjustments and warehouse restock movements.

2. **Eligibility Calculation**:
   - **14-day Window**: Calculated from delivery timestamp (`shipments.delivered_at`).
   - **Partial & Multi-Return Support**: Tracks cumulative returnable quantities across prior returns for every order item.
   - **Non-returnable Items**: Evaluates category-level policy flags (e.g. perishable items or downloadable digital goods).

3. **Return State Machine (17 States)**:
   `REQUESTED` → `UNDER_REVIEW` → `APPROVED` → `PICKUP_SCHEDULED` → `PICKED_UP` → `IN_TRANSIT` → `RECEIVED` → `INSPECTION_PENDING` → `INSPECTED` → `ACCEPTED` / `PARTIALLY_ACCEPTED` / `RETURN_REJECTED` → `REFUND_PENDING` → `REFUNDED` / `EXCEPTION`.

4. **Warehouse Inspection Outcomes**:
   - `ACCEPTED`: 100% item acceptance -> Full refund + Inventory restock.
   - `PARTIALLY_ACCEPTED`: Partial quantity acceptance -> Partial refund + Stock adjustment.
   - `RETURN_REJECTED`: QA failed -> 0 refund, no stock restoration.
   - `QUARANTINED` / `DAMAGED_SCRAP`: No stock restoration to sellable inventory.

5. **100% Idempotent Refund Execution**:
   - Unique database constraint `uq_refund_idempotency_key` (`RFD-{return_reference}`).
   - Ensures no duplicate refunds are submitted to external payment gateways (Razorpay, Stripe, PayPal).

---

## Architectural Data Flow
```
Customer / Admin
       |
       v
ReturnController / AdminReturnController
       |
       v
ReturnApplicationService -------------------> ReturnEligibilityService (14-day check)
       |
       +---> ReturnRepository (Spring Data JPA + Domain Aggregate)
       |
       +---> PaymentProvider (processRefund SPI)
       |
       +---> ApplicationEventPublisher
                  |
                  +---> ShipmentStatusEventListener (Reverse Logistics Booking)
                  +---> ReturnEventListener (Stock Restoration via InventoryApplicationService)
```

---

## Database Schema (Migration V10)
- `returns`: Aggregate root (`id`, `return_reference`, `order_id`, `status`, `reason_code`, `total_refundable_amount`, `@Version version`).
- `return_items`: Return line items (`id`, `return_id`, `order_item_id`, `sku`, `requested_quantity`, `approved_quantity`, `accepted_quantity`, `refund_amount`).
- `return_status_history`: Immutable transition audit log (`previous_status`, `new_status`, `reason`, `actor_type`, `actor_id`).
- `return_inspections`: Warehouse QA report (`inspector_id`, `outcome`, `notes`).
- `refund_records`: Transactional refund ledger (`refund_reference`, `payment_id`, `amount`, `status`, `idempotency_key`).
