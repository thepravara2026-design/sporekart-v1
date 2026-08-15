# SPOREKART v3.0 — STOCK RESERVATION & MOVEMENT LIFECYCLE

---

## 1. Reservation State Machine

```
                      +-------------------+
                      |      ACTIVE       |
                      +-------------------+
                        /       |       \
                       /        |        \
                      v         v         v
             +----------+ +-----------+ +----------+
             | RELEASED | | COMMITTED | | EXPIRED  |
             +----------+ +-----------+ +----------+
              (Terminal)   (Terminal)   (Terminal)
```

---

## 2. Movement Types

| Movement Type | Trigger Event | On-Hand Effect | Reserved Effect |
| :--- | :--- | :--- | :--- |
| `RESERVATION` | Order checkout stock reservation | Unchanged | `+ quantity` |
| `RELEASE` | Payment failure / customer cancellation | Unchanged | `- quantity` |
| `COMMIT` | Order fulfillment / shipment dispatch | `- quantity` | `- quantity` |
| `EXPIRY` | Reservation TTL expiration | Unchanged | `- quantity` |
| `STOCK_ADJUSTMENT` | Admin manual count adjustment | `new - prev` | Unchanged |
| `RESTOCK` | Return inspection ACCEPTED | `+ quantity` | Unchanged |
| `DAMAGE` | Return inspection DAMAGED / Warehouse defect | Unchanged (tracked in `damagedQuantity`) | Unchanged |
