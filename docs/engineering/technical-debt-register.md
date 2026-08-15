# SPOREKART v3.0 — Master Technical Debt Register

**Date**: 2026-08-15

---

## 1. Cataloged Technical Debt Items

| Debt ID | System Area | Description / Problem Statement | Impact | Priority | Estimated Effort | Target Sprint | Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| `DEBT-001` | Rate Limiting | In-memory sliding-window rate limiter stores state locally per instance | Limits multi-node horizontal scaling | Medium | 2 Days | Sprint 5A | CLASSIFIED |
| `DEBT-002` | Distributed Cache | Catalog query responses rely on DB query cache rather than Redis cluster | Higher DB CPU under > 50 req/sec catalog load | Low | 3 Days | Sprint 5B | CLASSIFIED |
| `DEBT-003` | Event Streaming | Outbox events processed via Spring `@EventListener` polling | Asynchronous event dispatch throughput cap | Low | 5 Days | Sprint 5C | CLASSIFIED |

---

## 2. Debt Management Policy
All cataloged technical debt items are **non-blocking** for current production release `v3.0.0` and are prioritized based on measurable scale thresholds.