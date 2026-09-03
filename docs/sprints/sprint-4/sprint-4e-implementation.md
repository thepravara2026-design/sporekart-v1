# SPOREKART v3.0 — SPRINT 4E IMPLEMENTATION DETAILS

---

### 1. Architectural Overview

Sprint 4E introduces a production-grade Inventory and Stock Accounting subsystem (`com.sporekart.modules.inventory`) supporting:
- **Net Available Accounting**: Formula `availableQuantity = onHandQuantity - reservedQuantity - damagedQuantity`.
- **Reservation Commitment (`commitReservation`)**: Commits reserved stock upon order fulfillment/delivery, deducting both `onHand` and `reserved` quantities and recording `MovementType.COMMIT`.
- **Damaged & Non-Sellable Stock (`recordDamagedStock`)**: Tracks non-sellable stock separately in `damagedQuantity` with `MovementType.DAMAGE` movements.
- **Append-Only Audit Ledger**: Every mutation records a row in `stock_movements`.
- **Deadlock-Free Concurrency**: Sorts SKUs alphabetically (`SKU ASC`) before obtaining pessimistic write locks (`SELECT ... FOR UPDATE`).
- **REST APIs**: `CustomerInventoryController` for customer availability checks (`IN_STOCK`, `LOW_STOCK`, `OUT_OF_STOCK`) and `AdminInventoryController` for inventory management and audit ledgers.

---

### 2. File Changes Summary

#### Backend (`com.sporekart.modules.inventory`)
- `V13__inventory_hardening.sql`: Flyway migration adding `damaged_quantity` and `low_stock_threshold` to `inventory_items`.
- `InventoryItem.java`: Domain aggregate updated with `commit()`, `recordDamaged()`, `isLowStock()`.
- `MovementType.java`: Added `COMMIT`, `RESTOCK`, `DAMAGE`.
- `StockReservation.java` & `ReservationStatus.java`: Added `COMMITTED` status and `commit()` transition.
- `InventoryApplicationService.java`: Added `commitReservation`, `recordDamagedStock`, `listMovementsForSku`, `getInventoryAvailability`, `listAllInventory`.
- `InventoryOrderEventListener.java`: Listens to `OrderLifecycleEvent` to release cancelled orders or commit shipped/delivered orders.
- `CustomerInventoryController.java` & `AdminInventoryController.java`: REST controllers for customer stock checks and admin inventory management.

#### Frontend (`frontend/src/`)
- `endpoints.ts`: Added inventory endpoints.
- `inventoryApi.ts`: Axios API service for stock availability checks, admin adjustments, damaged stock recording, and movement ledger queries.
