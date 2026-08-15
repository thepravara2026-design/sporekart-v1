# SPOREKART v3.0 — Rollback Decision Matrix

**Date**: 2026-08-15
**Sprint**: 4K Release Engineering

---

## 1. Rollback Trigger Criteria

| Trigger Symptom | Threshold / Metric | Severity | Automatic / Manual Decision | Rollback Action | Owner Role |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **High API Error Rate** | > 2.0% HTTP 5xx responses over 5 minutes | CRITICAL | Automatic / Lead SRE | Initiate blue/green application rollback to previous release tag | Lead SRE |
| **Payment Verification Outage** | > 5.0% payment verification failures | CRITICAL | Manual (SRE + Lead Dev) | Rollback application; trigger payment gateway circuit-breaker | Payment Lead |
| **Database Connection Saturation** | Hikari pool pending threads > 50 for 3 mins | CRITICAL | Automatic alert → Manual decision | Scale app instances down or rollback deploy if DB query regressed | Database Reliability Eng |
| **Inventory Corruption / Overselling** | Negative stock or duplicate reservation error | BLOCKER | Immediate Manual Execution | Rollback application; trigger inventory reconciliation job | Inventory Lead |
| **Data Migration Failure** | Flyway migration error during deploy | BLOCKER | Automatic deployment halt | Execute forward-fix script or restore pre-deploy DB snapshot | Lead DRE |
| **Authentication / IDOR Defect** | Access control failure or 401 spike (>10%) | BLOCKER | Immediate Manual Execution | Immediate rollback to previous release candidate tag | Security Engineer |

---

## 2. Rollback Execution Procedure

1. **Halt Rollout**: Stop any active deployment pipeline or canary traffic split immediately.
2. **Revert Traffic**: Route 100% of ingress load back to the previous stable release artifact.
3. **Database Strategy**:
   - If Flyway V16 migration succeeded without destructive schema alterations (all V16 changes are additive indexes and check constraints), **DO NOT roll back the database schema**. The schema remains backwards-compatible.
   - If data corruption occurred, restore database from the pre-deployment snapshot (`sporekart_pre_deploy.dump`).
4. **Verification**: Run post-rollback health checks and smoke test suite ([docs/release/production-smoke-test.md](file:///f:/sporekart-v3.0/docs/release/production-smoke-test.md)).