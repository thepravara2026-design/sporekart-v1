# SPOREKART v3.0 — Sprint 4 Architectural Handoff Package

## Handoff Overview & Domain Extension Points

---

### 1. Completed Sprint 3 Commerce Capabilities
SPOREKART v3.0 Sprint 3 delivers a complete, production-ready modular monolith commerce engine comprising 9 core domains:
1. **Catalog Domain**: Product catalog, category hierarchies, search, and variants.
2. **Cart Domain**: Persistent shopping carts, subtotals, and item management.
3. **Pricing Engine**: Authoritative server-side price recalculation and validation.
4. **Checkout Engine**: Preview computation and atomic order creation.
5. **Order Management**: Order persistence, customer order history, and cancellation.
6. **Inventory Reservation**: Pessimistic write locking, stock reservation ledgers, and TTL expiry handling.
7. **Payment Orchestration**: Payment attempt tracking, gateway callback idempotency, and webhook verification.
8. **Order State Machine**: Transition constraints (`CREATED` → `CONFIRMED` → `PROCESSING` → `READY_FOR_FULFILMENT` → `SHIPPED` → `DELIVERED`).
9. **Fulfilment & Returns**: Shipping handoffs, AWB booking, return inspection, and automated refund triggers.

---

### 2. Stable Domain Extension Points for Sprint 4

- **Payment Provider SDK Integration**: Interface `PaymentProviderPort` ready for live gateway integration (e.g., Stripe, Razorpay).
- **Outbox Event Bus Messaging**: Outbox table `outbox_events` ready to connect to external message broker (Apache Kafka or RabbitMQ).
- **Multi-Region Inventory Locking**: Interface `InventoryRepository` structured for distributed Redis lock implementation.
- **Analytics & Customer Loyalty**: Order event listeners (`OrderLifecycleEvent`) ready for analytics and rewards dispatch handlers.

---

### 3. Handoff Checklist
- [x] All 217 backend tests passing.
- [x] All 20 frontend tests passing.
- [x] Flyway migrations V1 through V11 verified.
- [x] Operational Runbook available at `docs/runbooks/commerce.md`.
- [x] Release Certification available at `docs/SPRINT_3_RELEASE_CERTIFICATION.md`.
