# SPOREKART v3.0 — INVENTORY & STOCK CONSISTENCY ARCHITECTURE

---

## 1. Subsystem Architecture

```
                          PRODUCT / CATALOG
                                  |
                                  v
                           INVENTORY DOMAIN
                     (InventoryItem Aggregate Root)
                                  |
     +----------------------------+----------------------------+
     |                            |                            |
     v                            v                            v
RESERVATION LIFECYCLE       MOVEMENT LEDGER            STOCK ADJUSTMENTS
 (Active/Released/          (Append-Only Audit          (Admin & Return
   Committed)                  History)                   Restocking)
```

---

## 2. Inventory Invariants

1. **Non-Negative Accounting**: `onHandQuantity >= 0`, `reservedQuantity >= 0`, `damagedQuantity >= 0`.
2. **Reservation Bound**: `reservedQuantity <= onHandQuantity`.
3. **Net Available Formula**: `availableQuantity = onHandQuantity - reservedQuantity - damagedQuantity`.
4. **Idempotent Reservations**: `reserveInventoryForOrder(orderId, customerId)` returns existing active reservation if re-submitted.
5. **Append-Only Movements**: Every stock mutation writes a row to `stock_movements`.
