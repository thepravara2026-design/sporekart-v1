# SPOREKART v3.0 — REFUND STATE MACHINE & IDEMPOTENCY MATRIX

---

## 1. Refund Status Lifecycle

```
                     +-------------------+
                     |      PENDING      |
                     +-------------------+
                               |
                               v
                     +-------------------+
                     |     INITIATED     |
                     +-------------------+
                        /             \
                       /               \
                      v                 v
            +-------------------+ +---------------+
            |    PROCESSING     | |    FAILED     | (Recoverable via retry)
            +-------------------+ +---------------+
                      |
                      v
            +-------------------+
            |     PROCESSED     |
            +-------------------+
                 (Terminal)
```

---

## 2. Status Mapping Table

| Internal Refund Status | Description | Provider State Mapping |
| :--- | :--- | :--- |
| `PENDING` | Refund record created locally prior to provider dispatch. | Initial local state |
| `INITIATED` / `PROCESSING` | Refund request submitted to Razorpay / Mock provider. | Razorpay `processed` / `pending` |
| `PROCESSED` | Payment provider confirmed refund completion. | Razorpay `processed` |
| `FAILED` | Payment provider rejected refund request (e.g. invalid payment ID, expired window). | Razorpay `failed` |

---

## 3. Financial Invariants

1. **Cumulative Limit**: `SUM(refund_records.amount WHERE status = 'PROCESSED' for order_id) <= order.total_amount`
2. **Positive Amount**: `amount > 0`
3. **Idempotency Key Uniqueness**: `idempotency_key = "RFD-" + returnReference` enforced via DB UNIQUE index.
4. **Precision**: All monetary calculations use `BigDecimal` with 2 decimal places (`HALF_UP`). Floating-point arithmetic is strictly forbidden.
