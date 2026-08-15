# 7. Order Lifecycle, State Machine & Order Orchestration

Date: 2026-08-15

## Status
Accepted

## Context
In SPOREKART v3.0, the Order domain must serve as the authoritative coordinator for the customer purchase journey, managing order states from creation through fulfilment and completion.

## Decision
1. **Explicit Domain State Machine**: Define `OrderStateMachine` with strict transition matrix validation (`CREATED` -> `PAYMENT_PENDING` -> `CONFIRMED` -> `PROCESSING` -> `READY_FOR_FULFILMENT` -> `SHIPPED` -> `OUT_FOR_DELIVERY` -> `DELIVERED` -> `COMPLETED`, terminal `CANCELLED`/`EXPIRED`).
2. **Immutable Audit Trail**: Append-only `order_status_history` table recording every state transition with reason, actor type (`CUSTOMER`, `ADMIN`, `SYSTEM`, `PAYMENT`, `SHIPPING`), actor ID, and timestamp.
3. **Optimistic Locking**: Add `@Version Long version` on `orders` table to guarantee thread-safe concurrent state transition protection.
4. **Side-Effect Orchestration**: Cancellation or expiration automatically releases active stock reservations via `InventoryApplicationService`.

## Consequences
- Prevents invalid or out-of-order status mutations.
- Full traceability for customer and administrative order history.
- 100% test coverage across state transitions, concurrency, security, and handoff contracts.
