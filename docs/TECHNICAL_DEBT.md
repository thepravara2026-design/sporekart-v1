# SPOREKART v3.0 — Technical Debt Log

The following non-blocking technical debt items are tracked for future sprints:

| Debt ID | Component | Severity | Description | Future Action / Target |
| :--- | :--- | :--- | :--- | :--- |
| **DEBT-001** | Payment Domain | P3 (Low) | Payment integration currently uses mock gateway provider implementation. | Integrate production Razorpay / Stripe SDK in future deployment sprint. |
| **DEBT-002** | Inventory TTL | P3 (Low) | Inventory reservation cleanup currently relies on application scheduler rather than distributed redis TTL lock. | Introduce Redis cluster for high-scale multi-region deployment. |
| **DEBT-003** | Outbox Event Processing | P3 (Low) | Outbox polling uses Spring `@Scheduled` in-process task worker. | Defer to external Kafka / RabbitMQ consumer workers at scale. |
