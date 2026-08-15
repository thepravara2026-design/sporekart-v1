# SPOREKART v3.0 — PRODUCTION ROLLBACK RUNBOOK

## 1. ROLLBACK TRIGGER CONDITIONS

Initiate a rollback immediately if any of the following conditions occur post-deployment:
1. Application readiness health probe (`/actuator/health/readiness`) fails for > 2 consecutive minutes.
2. HTTP 5xx error rate exceeds 2% of total traffic.
3. Database migration failure or connection pool exhaustion (`HikariPool - Connection is not available`).
4. Critical payment webhook processing failure impacting live transactions.

---

## 2. CONTAINER APPLICATION ROLLBACK PROCEDURE

If the issue is isolated to application logic and database schema was unchanged or backward-compatible:

### Step 1: Revert Container Image Tag to Previous Stable Tag
```bash
# Update docker-compose.prod.yml to reference previous stable image tag (e.g. sporekart-backend:2.9.0)
docker-compose -f docker-compose.prod.yml up -d --no-deps backend
```

### Step 2: Verify Rollback Health & Readiness
```bash
docker logs -f sporekart-backend
curl -f http://localhost:8080/actuator/health/readiness
```

---

## 3. DATABASE RECOVERY & FORWARD-FIX PROCEDURE

> [!WARNING]
> Flyway migrations are forward-only. Do NOT attempt to drop or alter database tables manually in production without approval.

If a database schema migration causes schema corruption or data loss:

### Option A: Forward-Fix Migration (Preferred)
1. Author a new Flyway migration `V20__fix_<issue>.sql` resolving the schema anomaly.
2. Test the forward migration in QAT/Staging environment.
3. Build a patch release container `sporekart-backend:3.0.1` and deploy via standard runbook.

### Option B: Full Database Restore (Emergency DR)
If data corruption occurred:
1. Stop incoming application traffic by scaling backend service to 0:
   ```bash
   docker-compose -f docker-compose.prod.yml stop backend
   ```
2. Restore database from pre-deployment backup:
   ```bash
   pg_restore -h <DB_HOST> -U <DB_USER> -d sporekart_prod --clean --if-exists /backups/sporekart_prod_pre_deploy_<timestamp>.dump
   ```
3. Deploy previous stable backend container image:
   ```bash
   docker-compose -f docker-compose.prod.yml up -d backend
   ```
4. Execute `PaymentReconciliationService` to reconcile any payment transactions that occurred during the incident window.

---

## 4. POST-ROLLBACK INCIDENT REVIEW

Document the incident root cause, metrics baseline prior to rollback, and remediation steps in an Incident Post-Mortem within 24 hours.
