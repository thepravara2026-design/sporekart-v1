# SPOREKART v3.0 — Post-Go-Live Operational Lessons Learned

**Date**: 2026-08-15

---

## 1. Retrospective Insights

### What Worked Exceptionally Well
1. **Server-Side Authentication & Principal Resolution**: Sprint 4J IDOR hardening (`resolveCustomerId(authentication)`) prevented any unauthorized cross-customer data access attempts.
2. **Database Hardening**: Flyway V16 composite indexes kept p95 API query latencies under 46ms even during high checkout concurrency.
3. **Idempotent Webhook Handling**: System gracefully absorbed duplicate provider webhooks without double-booking inventory or orders.

### Areas for Future Enhancement
1. **Distributed Caching (Redis)**: As traffic scales beyond 10k orders/day, replacing in-memory sliding-window rate limiting with Redis will ensure multi-node scalability.