# SPOREKART v3.0 — RETURN LIFECYCLE & STATE MACHINE SPECIFICATION

---

## 1. Canonical Return Lifecycle

```
    REQUESTED
       ↓
  UNDER_REVIEW  --------> REJECTED (Terminal)
       ↓
    APPROVED    --------> CANCELLED (Terminal)
       ↓
REVERSE_SHIPMENT_CREATED
       ↓
PICKUP_SCHEDULED
       ↓
   PICKED_UP
       ↓
   IN_TRANSIT
       ↓
    RECEIVED
       ↓
INSPECTION_PENDING
       ↓
   INSPECTED
       ↓
    ACCEPTED  ---------> RETURN_REJECTED (Terminal)
       ↓
 REFUND_PENDING
       ↓
    REFUNDED (Terminal)
```

---

## 2. Terminal States & Safeguards

- **REJECTED**: Initial return request denied during policy review.
- **CANCELLED**: Return request cancelled by customer before pickup.
- **RETURN_REJECTED**: Physical item failed quality inspection (e.g. counterfeit, missing tags, severe non-vendor damage).
- **REFUNDED**: Final completed state after successful gateway refund execution.
