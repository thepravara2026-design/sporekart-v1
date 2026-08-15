# SPOREKART v3.0 — CANONICAL RETURN STATE MACHINE & LIFECYCLE

---

## 1. Return Status Matrix

```
                          +-------------------+
                          |     REQUESTED     |
                          +-------------------+
                            /       |       \
                           /        |        \
                          v         v         v
            +---------------+  +----------+  +-----------+
            | UNDER_REVIEW  |  | APPROVED |  | CANCELLED | (Terminal)
            +---------------+  +----------+  +-----------+
               /         \          |
              v           v         v
     +----------+   +----------+ +--------------------------+
     | REJECTED |   | APPROVED | | REVERSE_SHIPMENT_CREATED |
     +----------+   +----------+ +--------------------------+
     (Terminal)                     |
                                    v
                         +-------------------+
                         | PICKUP_SCHEDULED  |
                         +-------------------+
                            /             \
                           v               v
                +-------------------+   +---------------+
                |     PICKED_UP     |   | PICKUP_FAILED |
                +-------------------+   +---------------+
                          |                     | (Retry/Reschedule)
                          v                     v
                +-------------------+   +---------------+
                |    IN_TRANSIT     |   |   APPROVED    |
                +-------------------+   +---------------+
                          |
                          v
                +-------------------+
                |     RECEIVED      |
                +-------------------+
                          |
                          v
                +-------------------+
                | INSPECTION_PENDING|
                +-------------------+
                          |
                          v
                +-------------------+
                |     INSPECTED     |
                +-------------------+
                   /        |        \
                  v         v         v
        +----------+ +--------------------+ +-----------------+
        | ACCEPTED | | PARTIALLY_ACCEPTED | | RETURN_REJECTED | (Terminal)
        +----------+ +--------------------+ +-----------------+
             \              /
              v            v
           +--------------------+
           |   REFUND_PENDING   |
           +--------------------+
               /             \
              v               v
       +------------+   +---------------+
       |  REFUNDED  |   |   EXCEPTION   | (Recoverable)
       +------------+   +---------------+
         (Terminal)             |
                                v
                        +---------------+
                        | REFUND_PENDING|
                        +---------------+
```

---

## 2. Allowed Transitions Rules

1. **`REQUESTED`**:
   - -> `UNDER_REVIEW`, `APPROVED`, `REJECTED`, `CANCELLED`
2. **`UNDER_REVIEW`**:
   - -> `APPROVED`, `REJECTED`, `CANCELLED`
3. **`APPROVED`**:
   - -> `REVERSE_SHIPMENT_CREATED`, `PICKUP_SCHEDULED`, `PICKED_UP`, `IN_TRANSIT`, `RECEIVED`, `CANCELLED`, `EXCEPTION`
4. **`REVERSE_SHIPMENT_CREATED`**:
   - -> `PICKUP_SCHEDULED`, `PICKED_UP`, `IN_TRANSIT`, `RECEIVED`, `PICKUP_FAILED`, `EXCEPTION`
5. **`PICKUP_SCHEDULED`**:
   - -> `PICKED_UP`, `IN_TRANSIT`, `RECEIVED`, `PICKUP_FAILED`, `EXCEPTION`
6. **`PICKUP_FAILED`**:
   - -> `PICKUP_SCHEDULED`, `APPROVED`, `CANCELLED`, `EXCEPTION`
7. **`PICKED_UP`**:
   - -> `IN_TRANSIT`, `RECEIVED`, `EXCEPTION`
8. **`IN_TRANSIT`**:
   - -> `RECEIVED`, `EXCEPTION`
9. **`RECEIVED`**:
   - -> `INSPECTION_PENDING`
10. **`INSPECTION_PENDING`**:
    - -> `INSPECTED`
11. **`INSPECTED`**:
    - -> `ACCEPTED`, `PARTIALLY_ACCEPTED`, `RETURN_REJECTED`
12. **`ACCEPTED` / `PARTIALLY_ACCEPTED`**:
    - -> `REFUND_PENDING`
13. **`REFUND_PENDING`**:
    - -> `REFUNDED`, `EXCEPTION`
14. **`EXCEPTION`**:
    - -> `REFUND_PENDING`, `APPROVED`

---

## 3. Terminal States

- `REJECTED`: Request rejected by admin prior to return approval.
- `CANCELLED`: Cancelled by customer prior to shipment pickup.
- `RETURN_REJECTED`: Rejected after warehouse inspection due to severe policy violation / damaged item.
- `REFUNDED`: Return completed and refund processed successfully.
