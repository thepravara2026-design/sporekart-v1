# SPOREKART v3.0 — Go-Live Execution Plan & Timeline

**Date**: 2026-08-15
**Version**: v3.0.0

---

## 1. Relative Go-Live Timeline

```
T-24h : Final Release Candidate Freeze & Pre-Deploy DB Snapshot
T-1h  : Maintenance Window Notification & Final Environment Audit
T-0   : Migration Execution & Application Deployment
T+15m : Smoke Testing & Initial Health Verification
T+30m : Switch Production Traffic to v3.0.0 Release Candidate
T+1h  : Initial Business Metric Verification (Orders, Payments, Registrations)
T+2h  : Primary Monitoring Check (Error Rate < 0.1%, Hikari Pool Healthy)
T+24h : Release Sign-Off & Post-Go-Live Report Publication
```

---

## 2. Step-by-Step Execution Plan

### Step 1: Database Pre-Deployment Snapshot (T-1h)
- **Action**: Run `pg_dump -Fc -U postgres sporekart_prod > /backups/sporekart_v3_pre_deploy.dump`
- **Verification**: Verify snapshot file size > 0 and checksum matches.
- **Failure Action**: Abort deployment; do not proceed without confirmed backup.

### Step 2: Database Migration Execution (T-0)
- **Action**: Execute Flyway migrations: `java -jar sporekart-backend.jar --spring.profiles.active=prod`
- **Verification**: Logs indicate: `Successfully applied 16 migrations to schema PUBLIC, now at version v16`.
- **Failure Action**: Execute rollback procedure per [docs/release/rollback-decision-matrix.md](file:///f:/sporekart-v3.0/docs/release/rollback-decision-matrix.md).

### Step 3: Application & Frontend Deployment (T+5m)
- **Action**: Start backend container/JAR and update Nginx/Gateway upstream to new release instance.
- **Verification**: `GET /actuator/health` returns `{"status":"UP"}`.

### Step 4: Production Smoke Testing (T+15m)
- **Action**: Run cURL production smoke test suite ([docs/release/production-smoke-test.md](file:///f:/sporekart-v3.0/docs/release/production-smoke-test.md)).
- **Verification**: All 9 cURL verification checks return expected 200/401 HTTP codes.