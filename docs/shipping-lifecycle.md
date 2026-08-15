# SPOREKART v3.0 — SHIPMENT LIFECYCLE & STATE MACHINE

---

## 1. Canonical Shipment Lifecycle

```
    CREATED
       ↓
READY_FOR_BOOKING
       ↓
BOOKING_PENDING
       ↓
    BOOKED  ------------+
       ↓                |
PICKUP_SCHEDULED        |
       ↓                v
   PICKED_UP ------> CANCELLED (Terminal)
       ↓
  IN_TRANSIT
       ↓
OUT_FOR_DELIVERY
       ↓
   DELIVERED (Terminal)
```

---

## 2. Failure & RTO Lifecycle

- **DELIVERY_FAILED**: Attempted delivery failure logged; triggers re-attempt or RTO.
- **RTO_INITIATED**: Return to Origin initiated by carrier.
- **RTO_IN_TRANSIT**: Shipment in reverse transit to warehouse.
- **RTO_DELIVERED**: Shipment delivered back to warehouse (Terminal).
