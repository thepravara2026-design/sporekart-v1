# SPOREKART v3.0 — Sprint 4A Implementation Summary

## Order Lifecycle & State Machine Subsystem

---

### Key Architectural Enforcements

1. **Single Authoritative State Machine (`OrderStateMachine.java`)**:
   - Enforces transition validity between `OrderStatus` states.
   - Rejects illegal state jumps (e.g. `CREATED` -> `DELIVERED`, `CANCELLED` -> `CONFIRMED`) with `InvalidOrderStateTransitionException`.
2. **Immutable Append-Only Audit History (`OrderStatusHistory.java`)**:
   - Every state change logs `previousStatus`, `newStatus`, `reason`, `actorType`, `actorId`, `correlationId`, `createdAt`.
   - Persistence is executed atomically alongside the order update using `saveAndFlush`.
3. **Generic & Operational Transition APIs (`AdminOrderController.java`)**:
   - Endpoint `POST /api/v1/admin/orders/{orderId}/transitions` allows generic administrative transition execution with validation.
   - Operational endpoints (`/process`, `/ready-for-fulfilment`, `/shipped`, `/out-for-delivery`, `/delivered`, `/complete`, `/cancel`) provide typed action handlers.
4. **Idempotency & Concurrency**:
   - Submitting the same transition repeatedly returns `OrderDto` without duplicate history entries.
   - JPA `@Version` optimistic locking protects against concurrent state updates.
5. **Inventory Release Compensation**:
   - Transitioning an order to `CANCELLED` or `EXPIRED` automatically invokes `InventoryApplicationService.releaseReservation` if an active stock reservation exists.
