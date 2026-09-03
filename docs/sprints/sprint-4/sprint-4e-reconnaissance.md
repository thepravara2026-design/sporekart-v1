# SPOREKART v3.0 — SPRINT 4E RECONNAISSANCE & ARCHITECTURE ANALYSIS

---

## 1. Executive Summary

Sprint 4E focuses on **Inventory, Stock Consistency & Fulfillment Orchestration**.
This document synthesizes our reconnaissance of the existing Sporekart codebase across Sprint 0 through Sprint 4D, outlining the exact inventory accounting model, Flyway database schemas (`V6`), stock reservation lifecycle, pessimistic write locking for concurrency, append-only stock movement ledger, return restocking boundaries, and admin inventory APIs.

---

## 2. Existing Codebase Reconnaissance & Findings

### 2.1 Database Schema (`V6__inventory_domain.sql`)
The inventory module comprises four database tables:
- `inventory_items`: `id`, `product_id`, `variant_id`, `sku`, `on_hand_quantity`, `reserved_quantity`, `status`, `version`, `created_at`, `updated_at`, with constraint `chk_reserved_le_onhand CHECK (reserved_quantity <= on_hand_quantity)`.
- `stock_reservations`: `id`, `reservation_reference`, `order_id`, `status`, `expires_at`, `release_reason`, `created_at`, `updated_at`.
- `stock_reservation_items`: `id`, `reservation_id`, `inventory_item_id`, `product_id`, `variant_id`, `sku`, `quantity`, `created_at`.
- `stock_movements`: `id`, `inventory_item_id`, `movement_type`, `quantity`, `reference_type`, `reference_id`, `previous_on_hand`, `resulting_on_hand`, `previous_reserved`, `resulting_reserved`, `created_at`.

### 2.2 Domain Boundaries & Integration Points
- **Order Domain (`com.sporekart.modules.order`)**: Authoritative for order state (`CREATED`, `CONFIRMED`, `CANCELLED`). Checkout invokes `reserveInventoryForOrder(orderId, customerId)`.
- **Payment Domain (`com.sporekart.modules.payment`)**: Payment failure or checkout cancellation triggers `releaseReservation(reservationId, reason)`.
- **Shipment Domain (`com.sporekart.modules.shipment`)**: Order fulfillment / shipment booking triggers stock commitment (`commitReservation`).
- **Return Domain (`com.sporekart.modules.returns`)**: Upon warehouse QA inspection acceptance (`ReturnAcceptedEvent`), `ReturnEventListener` invokes `adjustStock` to restock sellable items.
- **Inventory Domain (`com.sporekart.modules.inventory`)**: Authoritative for stock accounting, available quantity calculation (`onHand - reserved - damaged`), pessimistic write locking (`findAllBySkuInOrderBySkuAscForUpdate`), and append-only movement auditing (`StockMovement`).

---

## 3. Stock Accounting Model

| Category | Definition | Formula / Representation |
| :--- | :--- | :--- |
| **On-Hand Quantity** | Total physical quantity present in warehouse | `onHandQuantity` |
| **Reserved Quantity** | Stock locked for active checkout / order reservations | `reservedQuantity` |
| **Damaged Quantity** | Non-sellable damaged items awaiting salvage/disposition | `damagedQuantity` |
| **Available Quantity** | Net quantity available for new customer purchases | `onHandQuantity - reservedQuantity - damagedQuantity` |

---

## 4. Concurrency & Deadlock Prevention Strategy

1. **Deterministic Lock Ordering**: When reserving stock for multi-item orders, all requested SKUs are sorted in ascending order (`SKU ASC`) before acquiring pessimistic write locks (`SELECT ... FOR UPDATE`).
2. **Pessimistic Row Locking**: `findAllBySkuInOrderBySkuAscForUpdate` locks inventory items in database transactions to prevent overselling under high concurrency.
3. **Atomic Validation**: Availability is validated for all order line items before applying any reservation or stock deduction.

---

## 5. Identified Architectural Enhancements for Sprint 4E

1. **Stock Commitment (`commitReservation`)**:
   - Implement `commitReservation(UUID reservationId)`: Deducts both `onHandQuantity` and `reservedQuantity` by reserved amount upon order fulfillment. Records `MovementType.COMMIT`.
2. **Damaged / Non-Sellable Stock Accounting**:
   - Add `damagedQuantity` column to `inventory_items` and methods `recordDamagedStock(sku, quantity, reason)` in `InventoryApplicationService`.
3. **Append-Only Ledger Query & Low-Stock Detection**:
   - Implement `listMovementsForSku(String sku)` for audit trails.
   - Implement `lowStockThreshold` (default 5) and `isLowStock()` calculation.
4. **Customer Availability & Admin Inventory APIs**:
   - Add `GET /api/v1/inventory/skus/{sku}/availability` returning customer-safe status (`IN_STOCK`, `LOW_STOCK`, `OUT_OF_STOCK`).
   - Add `AdminInventoryController` endpoints for listing inventory, viewing movement ledger history, recording adjustments, and logging damaged items.

---

## 6. Migration & Safety Plan

- All Flyway migrations (`V1` through `V12`) run deterministically.
- New schema additions for `damaged_quantity` and `low_stock_threshold` are backwards-compatible with default values (`DEFAULT 0`).
- Verification across full test suites (`mvn clean test` and `npm run test -- --run` & `npm run build`).
