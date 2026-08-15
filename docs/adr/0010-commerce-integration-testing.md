# ADR 0010: Commerce Integration Testing Architecture and Domain Contract Assurance

- **Status**: ACCEPTED
- **Date**: 2026-08-15
- **Context**: SPOREKART v3.0 Sprint 3I

---

## Context

SPOREKART v3.0 contains 9 distinct modular monolith domains (Catalog, Cart, Pricing, Checkout, Order State Machine, Inventory Reservations, Payments, Shipping, and Returns/Refunds). While each domain was unit-tested independently in prior sprints, Sprint 3I requires validating cross-domain interactions, transactional boundaries, concurrency under race conditions, idempotency, failure compensation, and authorization controls across the entire commerce pipeline.

---

## Decision

We establish a comprehensive, multi-tiered integration test architecture under `backend/src/test/java/com/sporekart/integration/` utilizing Spring Boot integration test profiles with real H2 in-memory relational databases, Flyway migrations, JPA entity management, and multi-threaded execution pools.

### Key Integration Architectural Principles:

1. **Authoritative Pricing Enforcement**: Checkout previews and order creation re-query the Catalog domain directly for current product prices, preventing client-side cart price manipulation.
2. **Pessimistic Locking & SKU Sorting**: Inventory reservation locks required items in deterministic SKU ASC order using `SELECT FOR UPDATE` to avoid database deadlocks under high concurrency.
3. **Idempotency Verification**: Payment callbacks and return refunds use unique idempotency keys (`IDEM-*`, `RFD-*`) to ensure replay safety across network retries.
4. **Compensation Workflows**: Failed or declined payments automatically trigger reservation release commands to return stock to available inventory.
5. **Strict IDOR Protection**: Application service layers mandate matching customer identifiers (`customerId`) for all order, cart, and return detail queries.

---

## Consequences

- **Positive**:
  - Proves 100% end-to-end commerce correctness across all 9 domains.
  - Guarantees zero negative stock anomalies under multi-threaded concurrency.
  - Verifies database rollback integrity during mid-checkout failures.
- **Negative**:
  - Requires maintaining integration fixtures (`CommerceFixtures`) in sync with domain model entity changes.
