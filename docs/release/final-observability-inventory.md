# SPOREKART v3.0 — Final Observability Inventory

**Date**: 2026-08-15

---

## 1. Logging & MDC Context

- **Framework**: SLF4J + Logback
- **MDC Key Propagation**:
  - `requestId`: Generated per HTTP request in `RequestIdFilter` or extracted from `X-Request-ID` header.
  - `userId`: Extracted from authenticated Spring Security principal context.
- **Log Format Pattern**: `%5p [sporekart-backend,%X{requestId:-},%X{userId:-}]`

---

## 2. Health & Metrics Endpoints

- `GET /actuator/health`: Spring Boot Actuator health status (DB connection, disk space).
- `GET /actuator/info`: Application version & environment info.
- `GET /api/v1/version`: Public version contract returning build version & commit SHA.

---

## 3. Recommended Production Prometheus Alerts

| Alert Name | Condition | Severity | Runbook Link |
| :--- | :--- | :--- | :--- |
| `HighHttp5xxRate` | HTTP 5xx rate > 2% for 5 mins | CRITICAL | [docs/release/rollback-decision-matrix.md](file:///f:/sporekart-v3.0/docs/release/rollback-decision-matrix.md) |
| `HikariPoolExhaustion` | Hikari pending connections > 10 for 2 mins | HIGH | [docs/runbooks/rate-limiting-runbook.md](file:///f:/sporekart-v3.0/docs/runbooks/rate-limiting-runbook.md) |
| `RateLimitBreachSpike` | HTTP 429 count > 100/min | MEDIUM | [docs/runbooks/rate-limiting-runbook.md](file:///f:/sporekart-v3.0/docs/runbooks/rate-limiting-runbook.md) |