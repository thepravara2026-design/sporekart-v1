# SPOREKART v3.0 — Training Module Deployment Checklist

**Version**: Training 14 (Production Hardening & Final Acceptance)
**Applicable Deployments**: Any upgrade deploying Training 0–14

---

## Pre-Deployment Checklist

### Code & Tests
- [ ] `git status` → working tree is clean
- [ ] `git log --oneline -5` → confirm correct commits on branch
- [ ] `mvn test` → **0 test failures** (684+ tests passing)
- [ ] `npm run test -- --run` (frontend) → 30/30 passing
- [ ] No `TODO`, `FIXME`, or `HACK` markers in production source
- [ ] No secrets committed to source (`git grep -i "secret\|password\|apikey" src/main/`)

### Build Verification
- [ ] `mvn package -DskipTests` → JAR builds successfully
- [ ] `cd frontend && npm run build` → production bundle builds without warnings
- [ ] Docker image builds: `docker build -f infrastructure/docker/backend.Dockerfile ./backend`

### Database Migration
- [ ] V41 migration script reviewed and approved
- [ ] Migration tested against a staging DB snapshot
- [ ] `flyway validate` passes against target DB
- [ ] Rollback plan documented (see RECOVERY_PROCEDURES.md)

### Configuration
- [ ] `SPRING_PROFILES_ACTIVE=prod` set in production environment
- [ ] `JWT_SECRET_KEY` set to production value (min 512-bit HMAC-SHA512 key)
- [ ] `DATABASE_PASSWORD` set to strong production password
- [ ] `PAYMENT_PROVIDER_KEY` set to live key (not test/mock)
- [ ] `PAYMENT_PROVIDER_SECRET` set to live secret (not test/mock)
- [ ] `PAYMENT_WEBHOOK_SECRET` set to live webhook HMAC secret
- [ ] `CORS_ALLOWED_ORIGINS` scoped to production domain only
- [ ] `TRAINING_CANCELLATION_ADMIN_DAYS=7` confirmed
- [ ] `TRAINING_CANCELLATION_TRAINEE_DAYS=2` confirmed

### Flyway Migrations (V31–V41)
- [ ] V31: training_programs, training_batches, batch_schedules, training_enrollments — APPLIED
- [ ] V32: training_programs extended columns — APPLIED
- [ ] V33: batch_schedules / training_programs indexes — APPLIED
- [ ] V34: capacity invariant CHECK constraints — APPLIED
- [ ] V35: enrollment audit columns + idempotency key — APPLIED
- [ ] V36: training demand requests table — APPLIED
- [ ] V37: training_enrollment_payments + system order seed — APPLIED
- [ ] V38: enrollment lifecycle + history table — APPLIED
- [ ] V39: cancellation / rescheduling tables — APPLIED
- [ ] V40: reporting and audit indexes — APPLIED
- [ ] **V41: hardening constraints (actor NOT NULL, price check, indexes)** — **APPLY THIS DEPLOYMENT**

---

## Deployment Steps

1. **Snapshot DB** before deployment
2. **Stop traffic** (blue/green or canary, per deployment strategy)
3. **Deploy backend JAR** with updated environment variables
4. **Flyway auto-runs V41** on startup
5. **Health check**: `GET /actuator/health` → `{"status":"UP"}`
6. **Smoke test sequence** (see RUNBOOK.md §8)
7. **Resume traffic**

---

## Post-Deployment Verification

- [ ] `GET /actuator/health` → `{"status":"UP"}`
- [ ] `GET /actuator/health/trainingModule` → `{"status":"UP"}`
- [ ] `POST /api/v1/auth/login` → valid JWT returned
- [ ] `GET /api/v1/training-programs` → list returns with data
- [ ] Admin training overview report returns without error
- [ ] Zero `ERROR` level log entries in the first 5 minutes
- [ ] Payment provider connectivity verified (check provider dashboard)
- [ ] Verify V41 migration applied: `SELECT version FROM flyway_schema_history ORDER BY installed_rank DESC LIMIT 1;`

---

## Rollback Plan

> **Important**: V41 adds `NOT NULL` constraint on actor and structural CHECK constraints. If rollback is required, see RECOVERY_PROCEDURES.md → Flyway Rollback.

In emergencies:
1. Restore the DB snapshot taken before deployment
2. Deploy the previous JAR
3. Traffic returns to the prior version

---

## Contact Matrix

| Role | Responsibility |
|------|---------------|
| Engineering Lead | Migration approval, rollback decisions |
| DevOps/SRE | Environment variable management, deployment execution |
| QA | Smoke test sign-off, regression sign-off |
| Business Owner | Production go/no-go decision |
