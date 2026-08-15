# SPOREKART v3.0 — Go-Live Checklist

**Date**: 2026-08-15
**Target Release**: Sporekart v3.0.0

---

## 1. Pre-Go-Live Checklist (T-24h to T-1h)

- [x] **Release Candidate Frozen**: Code frozen on `release/sporekart-v3.0` at commit `2aeb2d0`.
- [x] **Automated Tests**: 256/256 tests passing clean (`mvn clean test`).
- [x] **Security Certification**: IDOR fixes, `SecurityConfig` tightening, CSP/HSTS headers, and rate limiter verified.
- [x] **Database Certification**: Flyway V16 applied; schema backup created (`sporekart_pre_deploy.dump`).
- [x] **Disaster Recovery Verified**: Restore test from pg_dump validated (RPO < 15m, RTO < 30m).
- [x] **Production Configuration Verified**: `application-prod.yml` validated; env vars cataloged in environment matrix.
- [x] **Secrets Injection**: Database credentials and API secrets bound to runtime env vars.
- [x] **Observability Verified**: MDC logging (`requestId`, `userId`) and `/actuator/health` checked.
- [x] **Smoke Tests Prepared**: Automated cURL smoke tests ready ([docs/release/production-smoke-test.md](file:///f:/sporekart-v3.0/docs/release/production-smoke-test.md)).
- [x] **Runbooks & Support Ready**: Admin Operations Guide and Support Readiness Guide published.

---

## 2. Go-Live Execution Checklist (T-0)

- [ ] **Step 1**: Take final pre-deployment database snapshot.
- [ ] **Step 2**: Apply Flyway database migrations (`V1` to `V16`).
- [ ] **Step 3**: Deploy production backend artifact (`sporekart-backend-0.1.0-SNAPSHOT.jar`).
- [ ] **Step 4**: Deploy production frontend static assets bundle.
- [ ] **Step 5**: Execute production smoke tests ([docs/release/production-smoke-test.md](file:///f:/sporekart-v3.0/docs/release/production-smoke-test.md)).
- [ ] **Step 6**: Route live production ingress traffic to v3.0.0 deployment.

---

## 3. Post-Go-Live Verification (T+15m to T+24h)

- [ ] **Step 7**: Monitor error rates (5xx < 0.1%) and latency (p95 < 150ms).
- [ ] **Step 8**: Verify live checkout, payment webhook, and shipment creation flows.
- [ ] **Step 9**: Conduct 24-hour observation window check and issue post-go-live report.