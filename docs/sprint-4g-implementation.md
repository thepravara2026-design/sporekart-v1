# SPOREKART v3.0 — SPRINT 4G IMPLEMENTATION DETAILS

---

### 1. Architectural Overview

Sprint 4G establishes a production-grade Returns, Reverse Logistics & Refund Orchestration subsystem (`com.sporekart.modules.returns`) supporting:
- **Return Aggregate & Policy Engine (`Return` & `ReturnEligibilityService`)**: Centralized return eligibility evaluation (`checkEligibility`), item selection, quantity ceiling enforcement, and state machine transitions (`ReturnStateMachine`).
- **Reverse Logistics Integration**: Coordinates reverse pickup transport with Sprint 4F `ShipmentApplicationService` provider abstraction.
- **Quality Inspection Workflow (`processInspection`)**: Admin quality inspection outcome (`ACCEPT`, `PARTIAL_ACCEPT`, `REJECT`) separating physical return receipt from inspection acceptance.
- **Refund Orchestration & Idempotency (`orchestrateRefund`)**: Idempotent gateway refund execution (`RefundRecordEntity` with `idempotencyKey = "RFD-" + returnReference` and `CONSTRAINT idempotency_key UNIQUE`).
- **Inventory Restock Integration (`InventoryReturnEventListener`)**: Listens to `ReturnAcceptedEvent` to automatically restock accepted sellable inventory (`RESTOCK` movement type) or account for damaged returns.
- **REST APIs**: Customer return APIs (`GET /api/v1/orders/{ref}/return-eligibility`, `POST /api/v1/orders/{ref}/returns`, `POST /api/v1/returns/{ref}/cancel`) and Admin return APIs (`GET /api/v1/admin/returns`, `POST /{ref}/approve`, `POST /{ref}/reject`, `POST /{ref}/inspect`, `POST /{ref}/refund/retry`, `POST /{ref}/create-reverse-shipment`, `POST /{ref}/sync`, `POST /{ref}/reconcile`).

---

### 2. File Changes Summary

#### Backend (`com.sporekart.modules.inventory` & `com.sporekart.modules.returns`)
- `InventoryReturnEventListener.java`: Event listener reacting to `ReturnAcceptedEvent` for automated inventory restocking.
- `AdminReturnController.java`: Added `POST /{returnReference}/sync` single-return sync endpoint.

#### Frontend (`frontend/src/`)
- `returnApi.ts`: Axios API service for return eligibility, return requests, tracking, cancellation, and admin return management.
