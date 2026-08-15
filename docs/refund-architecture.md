# SPOREKART v3.0 — REFUND ARCHITECTURE & IDEMPOTENCY SPECIFICATION

---

## 1. Refund Orchestration Flow

```
   Return Accepted
         ↓
  Calculate Refund
         ↓
Generate Idempotency Key ("RFD-" + returnReference)
         ↓
Check/Create RefundRecordEntity (PENDING)
         ↓
Invoke PaymentProvider.processRefund(...)
         ↓
  +------┴------+
  |             |
Success       Failure
  |             |
Update      Update Record (FAILED)
Record      Transition Return -> EXCEPTION
(PROCESSED)
  |
Mark Return
REFUNDED
```

---

## 2. Idempotency Safeguards

- **Key Generation**: `idempotencyKey = "RFD-" + returnReference`.
- **Database Uniqueness**: `refund_records` table enforces `CONSTRAINT idempotency_key UNIQUE`.
- **Replay Protection**: If `refund_records` exists with status `PROCESSED`, repeated orchestrations return the processed record without calling payment gateway again.
