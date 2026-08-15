# SPOREKART v3.0 — Commerce State Consistency Matrix

The following matrix documents the valid state combinations across Order, Payment, and Inventory domains.

---

### Valid Commerce State Combinations

| Order State | Payment State | Inventory Reservation State | System Description & Workflow Phase |
| :--- | :--- | :--- | :--- |
| `CREATED` | `PENDING` | `ACTIVE` | Initial checkout completed; stock reserved awaiting customer payment completion. |
| `CONFIRMED` | `CAPTURED` / `SUCCESS` | `CONFIRMED` | Payment verified successfully; inventory reservation locked permanently. |
| `PROCESSING` | `CAPTURED` / `SUCCESS` | `CONFIRMED` | Order handed off to warehouse fulfilment processing. |
| `READY_FOR_FULFILMENT` | `CAPTURED` / `SUCCESS` | `CONFIRMED` | Shipment created and assigned carrier tracking number. |
| `SHIPPED` | `CAPTURED` / `SUCCESS` | `FULFILLED` | Package dispatched with carrier; stock deducted from on-hand inventory. |
| `DELIVERED` | `CAPTURED` / `SUCCESS` | `FULFILLED` | Order delivered to customer. |
| `CANCELLED` | `FAILED` / `REFUNDED` | `RELEASED` | Order cancelled due to payment failure or customer request; reserved stock released. |

---

### Prevented Impossible States

1. **`CONFIRMED` order with `PENDING` payment**: Prevented by transaction state machine boundaries.
2. **`PAID` payment with `RELEASED` inventory**: Prevented by payment verification event listeners.
3. **Negative Available Stock**: Prevented by database `CHECK (on_hand_quantity >= 0)` constraints.
4. **Duplicate Financial Capture**: Prevented by idempotent callback processing using payment references.
