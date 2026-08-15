# SPOREKART v3.0 — Order Lifecycle & State Machine Specification

## 1. Overview
Sprint 4A establishes a single canonical, authoritative, production-grade Order Lifecycle & State Machine for the Sporekart platform. All order status mutations pass through strict domain validation, atomic transactional persistence, and append-only history audit tracking.

---

## 2. Canonical State Machine Graph

```text
                 ┌──────────────┐
                 │    CREATED   │
                 └──────┬───────┘
                        │
         ┌──────────────┼──────────────┐
         ↓              ↓              ↓
   ┌───────────┐  ┌───────────┐  ┌───────────┐
   │   PAID    │  │ CANCELLED │  │  EXPIRED  │
   └─────┬─────┘  └───────────┘  └───────────┘
         │
         ↓
   ┌───────────┐
   │ CONFIRMED │
   └─────┬─────┘
         │
         ↓
   ┌────────────┐
   │ PROCESSING │
   └─────┬──────┘
         │
         ↓
┌──────────────────────┐
│ READY_FOR_FULFILMENT │
└──────────┬───────────┘
           │
           ↓
     ┌───────────┐
     │  SHIPPED  │
     └─────┬─────┘
           │
           ↓
 ┌───────────────────┐
 │ OUT_FOR_DELIVERY  │
 └─────────┬─────────┘
           │
           ↓
     ┌───────────┐
     │ DELIVERED │
     └─────┬─────┘
           │
           ↓
     ┌───────────┐
     │ COMPLETED │
     └───────────┘
```

---

## 3. Transition Matrix & Permitted Actors

| Current State | Target State | Permitted Actors | Transition Source | System Effect / Side Effects |
| :--- | :--- | :--- | :--- | :--- |
| `CREATED` | `PAYMENT_PENDING` | `CUSTOMER`, `SYSTEM` | `CHECKOUT`, `PAYMENT` | Payment attempt created |
| `CREATED` / `PAYMENT_PENDING` | `PAID` | `PAYMENT` | `PAYMENT_EVENT` | Payment captured |
| `CREATED` / `PAYMENT_PENDING` / `PAID` | `CONFIRMED` | `PAYMENT`, `SYSTEM`, `ADMIN` | `PAYMENT_EVENT`, `ADMIN_API` | Stock reservation confirmed |
| `CREATED` / `PAYMENT_PENDING` / `CONFIRMED` | `CANCELLED` | `CUSTOMER`, `ADMIN` | `CUSTOMER_API`, `ADMIN_API` | Reserved stock released |
| `CREATED` / `PAYMENT_PENDING` | `EXPIRED` | `SYSTEM` | `INTERNAL_JOB` | Reservation window expired, stock released |
| `CONFIRMED` | `PROCESSING` | `ADMIN`, `SYSTEM` | `ADMIN_API` | Warehouse packing initiated |
| `PROCESSING` | `READY_FOR_FULFILMENT` | `ADMIN`, `SYSTEM` | `ADMIN_API` | Fulfilment prep complete |
| `READY_FOR_FULFILMENT` | `SHIPPED` | `SHIPPING`, `ADMIN` | `SHIPMENT_EVENT`, `ADMIN_API` | Tracking reference recorded |
| `SHIPPED` | `OUT_FOR_DELIVERY` | `SHIPPING`, `ADMIN` | `SHIPMENT_EVENT`, `ADMIN_API` | Package in courier transit |
| `OUT_FOR_DELIVERY` | `DELIVERED` | `SHIPPING`, `ADMIN` | `SHIPMENT_EVENT`, `ADMIN_API` | Delivered to customer |
| `DELIVERED` | `COMPLETED` | `ADMIN`, `SYSTEM` | `ADMIN_API`, `INTERNAL_JOB` | Order lifecycle finished |

---

## 4. Terminal State Invariants
- Terminal states: `CANCELLED`, `EXPIRED`, `COMPLETED`.
- Terminal states are strictly immutable. Any attempt to transition out of a terminal state throws `InvalidOrderStateTransitionException`.

---

## 5. Transactional & Audit Guarantees
1. **Atomic History**: Status update and `OrderStatusHistory` insertion are executed within a single `@Transactional` boundary (`saveAndFlush`).
2. **Optimistic Locking**: `@Version` field prevents lost updates under concurrent administrative transitions.
3. **Idempotency**: Transitioning an order to its current status returns `OrderDto` without creating duplicate history entries.
