# Production Deployment Checklist — SPOREKART v3.0

This checklist defines the mandatory operational gates for executing a production deployment of SPOREKART v3.0.

---

## 1. PRE-DEPLOYMENT GATES
- [ ] **Git Working Tree:** Clean status on release branch `sprint-6k-production-deployment`.
- [ ] **Release Artifacts:**
  - Backend JAR: `backend/target/sporekart-backend-0.1.0-SNAPSHOT.jar`
  - Frontend Bundle: `frontend/dist/`
- [ ] **Secret Externalization:** Zero secrets in Git or frontend static bundle.
- [ ] **Automated Test Verification:**
  - `ProductionDeploymentTestSuite`: 20 / 20 PASS
  - Backend Unit/Integration Tests: 319 / 319 PASS
  - Frontend Vitest Suite: 20 / 20 PASS
- [ ] **Database Pre-Check:** PostgreSQL / Supabase connection active and responsive.

---

## 2. DEPLOYMENT EXECUTION GATES
- [ ] **Environment Injection:** All required production variables configured (`SPRING_PROFILES_ACTIVE=prod`, `DATABASE_URL`, `JWT_SECRET_KEY`, `CORS_ALLOWED_ORIGINS`).
- [ ] **Flyway Migration:** Schema version v23 applied without validation errors.
- [ ] **Backend Process Launch:** Executable JAR running as non-root user (`sporekart:sporekart`).
- [ ] **CORS Origin Validation:** Restricted to production domain (`https://sporekart.com`).
- [ ] **Mock Isolation:** Production profile rejects mock provider fallback.

---

## 3. POST-DEPLOYMENT VERIFICATION GATES
- [ ] `/actuator/health` returns status `UP`.
- [ ] `/actuator/health/readiness` returns status `UP`.
- [ ] `/actuator/health/liveness` returns status `UP`.
- [ ] `/actuator/prometheus` scraping functional.
- [ ] Browser Release Smoke Test passed:
  - Storefront landing page loads cleanly
  - Catalog product listing renders
  - Product search & filter operational
  - Product detail view loads
  - Order creation and payment confirmation flows verified
- [ ] Response headers contain `X-Correlation-ID` and `X-Request-ID`.

---

## 4. ROLLBACK & RECOVERY GATES
- [ ] Previous stable container image tag identified and staged for instant rollback.
- [ ] Emergency stop script verified (`docker-compose -f docker-compose.prod.yml stop`).
- [ ] Incident logging procedure activated in `docs/operations/production-incident-register.md`.
