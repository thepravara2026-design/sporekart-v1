# SPOREKART v3.0 — Sprint 4A Repository Reconnaissance & Architecture Document

## Order Lifecycle & State Machine Architecture Analysis

---

### Executive Summary

Reconnaissance of the SPOREKART v3.0 codebase confirms that a modular monolith Order domain exists under `backend/src/main/java/com/sporekart/modules/order/`. The Order aggregate (`Order.java`), Order state machine (`OrderStateMachine.java`), status history model (`OrderStatusHistory.java`), and application service (`OrderApplicationService.java`) already enforce strict domain state transitions, transactional history persistence, and correlation context tracking.

---

### 1. Existing Order Domain & Schema Inspection

#### Domain Model (`com.sporekart.modules.order.domain`)
- **`Order`**: Aggregate root containing `id`, `orderNumber`, `customerId`, `status`, `currency`, line items, financial snapshots, `version`, `createdAt`, `updatedAt`.
- **`OrderStatus`**: `CREATED`, `PAYMENT_PENDING`, `PAID`, `CONFIRMED`, `PROCESSING`, `READY_FOR_FULFILMENT`, `SHIPPED`, `OUT_FOR_DELIVERY`, `DELIVERED`, `COMPLETED`, `CANCELLED`, `PAYMENT_FAILED`, `EXPIRED`.
- **`OrderStateMachine`**: Authoritative transition matrix enforcing allowed status jumps and rejecting illegal state jumps with `InvalidOrderStateTransitionException`.
- **`OrderStatusHistory`**: Immutable append-only audit trail capturing `previousStatus`, `newStatus`, `reason`, `actorType` (`CUSTOMER`, `ADMIN`, `SYSTEM`, `PAYMENT`, `SHIPPING`), `actorId`, `correlationId`, `createdAt`.

#### Database Schema (`orders`, `order_items`, `order_status_history`)
- `orders` table (Flyway `V5__order_domain.sql` & `V8__order_lifecycle_audit.sql`): `id`, `order_number` (UNIQUE), `customer_id`, `status`, `version`, `created_at`, `updated_at`.
- `order_status_history` table: `id`, `order_id` (FK to `orders`), `previous_status`, `new_status`, `reason`, `actor_type`, `actor_id`, `correlation_id`, `created_at`. Indexed on `(order_id, created_at DESC)`.

---

### 2. State Transition Graph Analysis

```
       ┌────────────────────────┐
       │        CREATED         │
       └───────────┬────────────┘
                   │
         ┌─────────┴─────────┐
         ↓                   ↓
   ┌───────────┐       ┌───────────┐
   │   PAID    │       │ CANCELLED │
   └─────┬─────┘       └───────────┘
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

### 3. Verification & Sprint 4A Refinement Plan

1. **Explicit Generic Transition Endpoint**: Expose `POST /api/v1/orders/{orderId}/transitions` for explicit administrative and system lifecycle transitions with `targetStatus`, `reason`, `actorType`, and `actorId` context.
2. **Idempotency & Concurrency Hardening**: Ensure duplicate transition requests return deterministic `OrderDto` without duplicate history creation or version conflict.
3. **Transactional Event Publishing**: Publish `OrderLifecycleEvent` atomically after status change commitment.
4. **Comprehensive Test Suite**: Verify valid transitions, invalid transitions, terminal state protection, customer ownership authorization, multi-threaded concurrency, and idempotency.
