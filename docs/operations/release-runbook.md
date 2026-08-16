# Release Runbook — SPOREKART v3.0

This runbook documents the release lifecycle, quality gates, versioning strategy, and release verification process for SPOREKART v3.0.

---

## 1. Release Lifecycle & Quality Gates

```
  Source Checkout
        ↓
  Maven Backend Build & Packaging
        ↓
  Vite Frontend Asset Build
        ↓
  Automated Test Execution (Unit, Integration, Hardening Suites)
        ↓
  Release Metadata Generation (build-info.properties & release-manifest.json)
        ↓
  Deployment to Staging / Production-like Target
        ↓
  Flyway Schema Migration & Validation
        ↓
  Readiness Probe Verification
        ↓
  Browser Smoke Test Verification
        ↓
  Release Approval & Sign-Off
```

---

## 2. Quality Gate Thresholds

All releases must satisfy 100% of the following automated quality gates prior to production deployment:

1. **Backend Tests:** 319 / 319 PASSED (`mvn test`)
2. **Frontend Tests:** 20 / 20 PASSED (`npm test -- --run`)
3. **Hardening Test Suites:**
   - Security: 35 / 35 PASSED
   - API Contract: 25 / 25 PASSED
   - Persistence: 18 / 18 PASSED
   - Catalog: 15 / 15 PASSED
   - Provider Reliability: 20 / 20 PASSED
   - Observability: 20 / 20 PASSED
   - Release Readiness: 20 / 20 PASSED
4. **Defect Threshold:** 0 P0 / P1 / P2 defects.

---

## 3. Versioning & Artifact Traceability

- **Release Artifact Versioning:** Semantic Versioning (`v3.0.0`) combined with Git Commit SHA.
- **Actuator Metadata:** Embedded in `/actuator/info` via Spring Boot `build-info.properties`.
- **Traceability Question:** *"Which Git commit produced this deployment?"*
  - Query `/actuator/info` or inspect `release-manifest.json`.

---

## 4. Post-Release Monitoring & Rollback Criteria

Monitor the platform for 60 minutes post-release:
- **Error Rate Alert:** HTTP 5xx rate > 0.5% triggers automated investigation.
- **P95 Latency Alert:** API response duration > 1000ms triggers performance review.
- **Provider Callback Failures:** Webhook failure count > 0 triggers provider reliability inspection.
- **Rollback Trigger:** Persistent readiness probe failure or P0 payment/order corruption triggers instant rollback to previous container tag.
