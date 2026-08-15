# SPOREKART v3.0 — RETURNS & REFUNDS DOMAIN ARCHITECTURE

---

## 1. Subsystem Architecture

```
                       ORDER DOMAIN
                            |
                            v
                      RETURN DOMAIN
                 (Return Aggregate Root)
                            |
      +---------------------+---------------------+
      |                     |                     |
      v                     v                     v
REVERSE LOGISTICS       INSPECTION         REFUND ORCHESTRATOR
 (Shipping SPI)      (Quality Outcome)    (Payment Refund SPI)
                            |                     |
                            v                     v
                    INVENTORY RESTOCK       GATEWAY REFUND
                     (Movement Ledger)       (Idempotent Key)
```

---

## 2. Core Domain Invariants

1. **Domain Isolation**: Return coordinates post-purchase workflows; Shipping owns transport; Payment owns gateway refund execution; Inventory owns stock ledgers.
2. **Quantity Ceiling Protection**: Requested return quantity cannot exceed delivered order quantity minus previously returned/pending quantities.
3. **Idempotent Refund Execution**: Refund records enforce UNIQUE `idempotency_key` constraint preventing duplicate gateway refunds.
4. **Receipt vs Acceptance**: Physical return receipt (`RECEIVED`) is distinct from quality inspection acceptance (`ACCEPTED` / `PARTIALLY_ACCEPTED`). Restocking and refunds only trigger on inspection acceptance.
