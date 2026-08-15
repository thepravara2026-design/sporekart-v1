# ADR 0005: Inventory Reservation & Stock Consistency

## Status
Accepted

## Context
During checkout and order processing, multiple customers may concurrently attempt to purchase popular items. Without robust inventory reservation and concurrency controls, overselling can occur, leading to bad customer experience and order fulfillment failures.

## Decision
1. **Separate Inventory Subsystem**: Inventory is maintained in a dedicated module (`com.sporekart.modules.inventory`) with authoritative physical stock levels (`on_hand_quantity`, `reserved_quantity`). Catalog contains product metadata only.
2. **Database Concurrency Control**: Stock reservations acquire pessimistic write locks (`SELECT FOR UPDATE`) on inventory rows using **deterministic SKU sorting** (`ORDER BY sku ASC`) to prevent deadlocks across concurrent multi-item checkouts.
3. **Invariants Enforced at DB and Domain Level**:
   - `availableQuantity = onHandQuantity - reservedQuantity >= 0`
   - `reservedQuantity <= onHandQuantity`
4. **Idempotency & Expiry**:
   - Reservations are tied uniquely to `orderId`. Retrying reservation returns the active reservation without double-reserving stock.
   - Reservations have configurable TTL (default 15 minutes) and are automatically released by a background `@Scheduled` cleanup job (`ReservationExpiryScheduler`).
5. **Immutable Audit Movements**: All stock changes are recorded in `stock_movements` for full traceability.

## Consequences
- **Positive**: 0 overselling under high concurrency, 0 deadlock risk, complete auditability, clean handoff for Payment (3E) and State Machine (3F).
- **Negative**: Requires maintaining inventory tables alongside Catalog SKU definitions.
