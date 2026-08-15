# SPOREKART v3.0 — INVENTORY CONCURRENCY & DEADLOCK PREVENTION

---

## 1. Concurrency Principles

1. **Pessimistic Locking**: `findAllBySkuInOrderBySkuAscForUpdate` executes `SELECT ... FOR UPDATE` on `inventory_items` rows inside Spring `@Transactional` boundaries.
2. **Deterministic Lock Hierarchy**: All multi-item SKU list queries sort SKUs in natural alphabetical order (`SKU ASC`). This guarantees that concurrent transactions reserving overlapping item sets (e.g. Transaction A reserving `SKU-1`, `SKU-2` and Transaction B reserving `SKU-2`, `SKU-1`) acquire locks in identical sequence, completely eliminating database deadlocks.
3. **Atomic Availability Validation**: Stock availability is evaluated for all order line items inside the lock boundary before mutating `reservedQuantity`. If any SKU has `availableQuantity < requestedQuantity`, `InsufficientStockException` is thrown and no partial reservations occur.
