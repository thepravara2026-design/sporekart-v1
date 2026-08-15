# SPOREKART v3.0 — Future Scale Recommendations

**Date**: 2026-08-15
**Scope**: Post Go-Live Architectural Roadmap (Non-blocking for v3.0.0 release)

---

## 1. Evidence-Based Scale Recommendations

1. **Redis Distributed Caching & Rate Limiting (DEBT-002)**:
   - When horizontal scaling exceeds 2 active instances, replace in-memory `RateLimitingFilter` with a Redis sliding window filter (`Bucket4j-Redis` or Spring Cloud Gateway RateLimiter).
   - Implement Redis read-aside caching for `GET /api/v1/catalog/products` and `/rating-summary` to offload PostgreSQL read IOPS.

2. **Database Read Replicas**:
   - Introduce PostgreSQL Read Replicas for heavy analytical and customer order history queries (`GET /api/v1/orders` & `/admin/analytics`).

3. **Transactional Outbox / Kafka Event Streaming (DEBT-003)**:
   - Upgrade Spring `@TransactionalEventListener` outbox implementation to an external Debezium/Kafka event bus when domain event throughput exceeds 10,000 events/sec.