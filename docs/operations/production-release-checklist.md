# Production Release Checklist — SPOREKART v3.0

This document defines the mandatory pre-deployment, deployment, post-deployment, and rollback quality gate checklist for deploying SPOREKART v3.0 to production.

---

## 1. Pre-Deployment Verification
- [ ] Source tree clean on target release branch (`git status`).
- [ ] Maven clean compile and packaging succeeds (`mvn clean package`).
- [ ] Frontend TypeScript compile and production asset build succeeds (`npm run build`).
- [ ] Full backend regression test suite passes (319/319 tests).
- [ ] Frontend Vitest suite passes (20/20 tests).
- [ ] Hardening suites pass (Security 35/35, Contract 25/25, Persistence 18/18, Catalog 15/15, Provider 20/20, Observability 20/20, Release 20/20).
- [ ] Zero secret credentials checked into repository or bundled in static frontend assets.
- [ ] Production environment secrets configured in external secrets manager (PostgreSQL, JWT secret, Razorpay, Shiprocket).
- [ ] Flyway database migrations (V1 through V23) validated against target PostgreSQL instance.

---

## 2. Deployment Execution
- [ ] Activate `prod` profile (`SPRING_PROFILES_ACTIVE=prod`).
- [ ] Verify CORS allowed origins setting excludes wildcard `*`.
- [ ] Execute Flyway database schema migration (`spring.flyway.enabled=true`).
- [ ] Launch backend container with non-root user `sporekart:sporekart`.
- [ ] Start static asset server (Nginx container serving `frontend/dist/`).
- [ ] Monitor process startup logs for zero `ERROR` or `CRITICAL` exceptions.

---

## 3. Post-Deployment Verification
- [ ] Probe `/actuator/health` endpoint returns `{"status":"UP"}`.
- [ ] Probe `/actuator/health/liveness` returns `UP`.
- [ ] Probe `/actuator/health/readiness` returns `UP`.
- [ ] Verify Prometheus scrape endpoint `/actuator/prometheus` returns HTTP 200.
- [ ] Execute browser smoke test against production domain:
  - Landing page loads
  - Catalog product grid displays
  - Product search and filter functional
  - Product detail view renders
  - Health page reflects system operational state
- [ ] Confirm request tracing headers `X-Correlation-ID` and `X-Request-ID` are present in response headers and structured MDC logs.

---

## 4. Rollback Readiness & Trigger Criteria
- [ ] Deployment triggers automatic rollback if:
  - Backend readiness probe fails after 3 retries (30s)
  - Flyway migration fails validation
  - Unhandled 5xx API errors exceed 1% threshold
  - Payment or Shipping provider webhook callbacks fail
- [ ] Emergency rollback procedure:
  - Stop failing release container
  - Restore previous stable backend container artifact
  - Re-verify `/actuator/health/readiness`
  - Notify DevOps and release engineers
