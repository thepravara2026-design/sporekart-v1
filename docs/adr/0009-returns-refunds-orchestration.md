# 0009. Returns, Reverse Logistics & Refund Orchestration Subsystem

## Status
Accepted

## Context
SPOREKART v3.0 requires a production-grade, domain-driven subsystem for customer returns, reverse logistics, warehouse inspection, stock restoration, and financial refund execution. Prior sprints established strict bounded contexts:
- Sprint 3F owns Order lifecycle & original financial record.
- Sprint 3G owns Shipping & fulfilment orchestration.
- Sprint 3E owns Payment processing.
- Sprint 2 & 3D own Inventory reservations and stock movements.

The Return subsystem must handle reverse logistics and refunds without violating domain boundaries or introducing duplicate payment/refund execution bugs.

## Decision
1. **Domain Boundary Preservation**:
   - The Return module acts as a domain orchestrator. It does NOT directly mutate Order, Payment, or Inventory database tables.
   - Financial refunds are executed exclusively by extending the `PaymentProvider` SPI with `processRefund(PaymentRefundRequest)`.
   - Reverse shipments are created by invoking `ShipmentApplicationService` from Sprint 3G.
   - Stock restoration is triggered via `ReturnAcceptedEvent` and handled by `InventoryApplicationService`.

2. **Idempotency Guarantee**:
   - Each refund operation generates a deterministic idempotency key (`RFD-{return_reference}`).
   - The `refund_records` table enforces a database-level unique constraint (`uq_refund_idempotency_key`) to prevent duplicate payouts.

3. **17-State Domain State Machine**:
   - Implemented `ReturnStateMachine` validating state transitions to prevent terminal state regressions and invalid lifecycle progressions.

4. **Multi-Return & Partial Quantity Accounting**:
   - `ReturnEligibilityService` calculates returnable quantities by evaluating original order quantities against cumulative prior approved returns.

## Consequences
- Clean separation of concerns between returns, logistics, payments, and inventory.
- Complete financial safety and idempotency against gateway retries.
- High testability across domain units, concurrency scenarios, and end-to-end integration workflows.
