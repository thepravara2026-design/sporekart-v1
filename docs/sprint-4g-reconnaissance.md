# SPOREKART v3.0 — SPRINT 4G RECONNAISSANCE & ARCHITECTURE ANALYSIS

---

## 1. Executive Summary

Sprint 4G focuses on **Returns, Reverse Logistics & Refund Orchestration**.
This document synthesizes our reconnaissance of the existing Sporekart codebase across Sprint 0 through Sprint 4F, detailing the return aggregate (`Return`), eligibility engine (`ReturnEligibilityService`), reverse logistics integration with Sprint 4F `ShipmentApplicationService`, refund orchestrator (`orchestrateRefund`), Flyway database schemas (`V10`), inventory restocking boundaries, idempotency controls (`idempotencyKey`), and REST controllers.

---

## 2. Existing Codebase Reconnaissance & Findings

### 2.1 Database Schema (`V10__returns_refunds_domain.sql`)
The returns & refunds subsystem comprises five database tables:
- `returns`: `id`, `return_reference`, `order_id`, `order_reference`, `customer_id`, `status`, `reason_code`, `reason_description`, `evidence_urls`, `policy_version`, `requested_at`, `approved_at`, `received_at`, `inspected_at`, `completed_at`, `reverse_shipment_id`, `version`.
- `return_items`: `id`, `return_id`, `order_item_id`, `product_id`, `sku`, `product_name_snapshot`, `requested_quantity`, `approved_quantity`, `received_quantity`, `accepted_quantity`, `rejected_quantity`, `unit_price`, `refund_amount`, `reason_code`.
- `return_status_history`: `id`, `return_id`, `previous_status`, `new_status`, `reason`, `actor_type`, `actor_id`, `correlation_id`.
- `return_inspections`: `id`, `return_id`, `inspector_id`, `outcome`, `notes`, `inspected_at`.
- `refund_records`: `id`, `refund_reference`, `return_id`, `return_reference`, `order_id`, `customer_id`, `payment_id`, `payment_reference`, `provider`, `amount`, `currency`, `status`, `failure_reason`, `provider_refund_id`, with `CONSTRAINT idempotency_key UNIQUE`.

### 2.2 Domain Boundaries & Subsystem Interplays
- **Return Domain (`com.sporekart.modules.returns`)**: Authoritative for return requests (`Return.createNewRequest`), return eligibility evaluation (`ReturnEligibilityService`), item selection & quantity validation, state machine transitions (`ReturnStateMachine`), inspection processing (`processInspection`), and refund orchestration (`orchestrateRefund`).
- **Payment Domain (`com.sporekart.modules.payment`)**: `PaymentProvider.processRefund` executes gateway refund logic idempotently.
- **Inventory Domain (`com.sporekart.modules.inventory`)**: Listens to `ReturnAcceptedEvent` via `InventoryReturnEventListener` to perform sellable restocking (`RESTOCK` movement) or non-sellable accounting (`DAMAGE` movement).
- **Shipping Domain (`com.sporekart.modules.shipment`)**: Provides carrier abstraction for reverse shipment transport without duplicating carrier logic inside Returns.

---

## 3. Return & Refund Architecture Diagram

```
                    ORDER DOMAIN
                         |
                         v
                   RETURN DOMAIN
              (Return Aggregate Root)
                         |
      +------------------+------------------+
      |                  |                  |
      v                  v                  v
REVERSE LOGISTICS    INSPECTION       REFUND ORCHESTRATOR
 (Shipping SPI)    (Quality Check)    (Payment Refund SPI)
                         |                  |
                         v                  v
                 INVENTORY RESTOCK    RAZORPAY / MOCK
                    (Restock /          GATEWAY REFUND
                  Damaged Ledger)
```

---

## 4. Identified Architectural Enhancements for Sprint 4G

1. **Inventory Restock Integration Listener (`InventoryReturnEventListener`)**:
   - Implement `InventoryReturnEventListener` in `com.sporekart.modules.inventory.infrastructure.listener` listening to `ReturnAcceptedEvent` to automatically handle inventory restocking (`RESTOCK` / `DAMAGE`).
2. **Frontend Service (`returnApi.ts`)**:
   - Create `frontend/src/services/returnApi.ts` wrapping customer return eligibility check, request creation, tracking, cancellation, and admin operations.

---

## 5. Verification & Safety Plan

- Flyway migrations (`V1` through `V13`) verified.
- Full verification across backend (`mvn clean test`), frontend unit tests (`npm run test -- --run`), and production build (`npm run build`).
