# SPOREKART v3.0 — Training Module Operational Runbook

**Version**: Training 14 (Production Hardening & Final Acceptance)
**Last Updated**: 2026-08-17
**Scope**: Training Module (Training 0–14)

---

## 1. System Health

### Check Training Module Health
```bash
curl -s http://localhost:8080/actuator/health/trainingModule | jq .
```
Expected: `{"status":"UP","details":{"trainingProgramsAccessible":true}}`

### Check Full Application Health
```bash
curl -s http://localhost:8080/actuator/health | jq .
```
Verify `status: UP` for: `db`, `trainingModule`, `diskSpace`, `ping`

### Check Application Metrics
```bash
curl -s http://localhost:8080/actuator/metrics | jq '.names[]' | grep -i training
```

---

## 2. Inspecting Failed Payments

### Identify payments stuck in PENDING or VERIFIED state
```sql
SELECT id, batch_id, trainee_id, amount, currency, status, created_at, updated_at
FROM training_enrollment_payments
WHERE status IN ('PENDING', 'VERIFIED', 'ENROLLMENT_PENDING')
ORDER BY created_at DESC;
```

### Inspect a specific payment by ID
```sql
SELECT * FROM training_enrollment_payments WHERE id = '<payment_id>';
```

### Find enrollments without a corresponding ENROLLMENT_CONFIRMED payment
```sql
SELECT e.id, e.batch_id, e.trainee_id, e.status
FROM training_enrollments e
WHERE e.status = 'CONFIRMED'
AND NOT EXISTS (
    SELECT 1 FROM training_enrollment_payments p
    WHERE p.enrollment_id = e.id
    AND p.status = 'ENROLLMENT_CONFIRMED'
);
```

### Recovery: Admin Retry
Use the Admin Reporting Console: `POST /api/v1/admin/training/reports/controls/retry-notification/{id}`

---

## 3. Inspecting Failed Notifications

### View failed notifications in the exception queue
```
GET /api/v1/admin/training/reports/exceptions
```

### From database
```sql
SELECT id, type, channel, status, retry_count, created_at, last_attempted_at
FROM notifications
WHERE status = 'FAILED'
AND created_at > NOW() - INTERVAL '24 hours'
ORDER BY last_attempted_at DESC;
```

### Admin Retry via API
```
POST /api/v1/admin/training/reports/controls/retry-notification/{enrollmentId}
Authorization: Bearer <admin-token>
```

---

## 4. Inspecting Failed Certificates

### Find completed enrollments without a certificate
```sql
SELECT e.id, e.trainee_id, e.batch_id, e.completed_at
FROM training_enrollments e
WHERE e.status = 'COMPLETED'
AND NOT EXISTS (
    SELECT 1 FROM training_certificates c WHERE c.enrollment_id = e.id
);
```

### Admin Certificate Retry
```
POST /api/v1/admin/training/reports/controls/retry-certificate/{enrollmentId}
Authorization: Bearer <admin-token>
```

---

## 5. Inspecting Audit History

### Search audit history by enrollment
```
GET /api/v1/admin/training/reports/audit?enrollmentId={id}
Authorization: Bearer <admin-token>
```

### From database
```sql
SELECT h.id, h.enrollment_id, h.from_status, h.to_status, h.reason, h.actor, h.created_at
FROM training_enrollment_history h
WHERE h.enrollment_id = '<enrollment_id>'
ORDER BY h.created_at ASC;
```

**Audit records are immutable.** No UPDATE or DELETE is possible through application APIs.

---

## 6. Inspecting Full Batches

### Find all FULL or near-full batches
```sql
SELECT id, batch_code, program_id, status, occupied_seats, total_capacity,
       ROUND(100.0 * occupied_seats / NULLIF(total_capacity, 0), 1) AS utilization_pct
FROM training_batches
WHERE occupied_seats >= total_capacity * 0.8
ORDER BY utilization_pct DESC;
```

### Find demand queue for a full batch
```sql
SELECT COUNT(*) FROM training_demand_requests
WHERE batch_id = '<batch_id>'
AND status = 'PENDING';
```

### Admin: Increase Batch Capacity
```
PATCH /api/v1/admin/batches/{batchId}/capacity
Authorization: Bearer <admin-token>
Content-Type: application/json
{"totalCapacity": <new_value>}
```

---

## 7. Capacity Inconsistency Recovery

If `occupied_seats` is inconsistent with actual active enrollments:

### Step 1: Audit
```sql
SELECT
    b.id, b.batch_code, b.occupied_seats AS stored_count,
    COUNT(e.id) AS actual_count,
    b.occupied_seats - COUNT(e.id) AS discrepancy
FROM training_batches b
LEFT JOIN training_enrollments e ON e.batch_id = b.id
    AND e.status IN ('CONFIRMED', 'ACTIVE', 'COMPLETED')
GROUP BY b.id, b.batch_code, b.occupied_seats
HAVING b.occupied_seats != COUNT(e.id);
```

### Step 2: If discrepancy found, escalate to engineering
Do NOT manually update `occupied_seats` without engineering review. Changes must be:
- Idempotent
- Atomic (within a transaction)
- Audit-logged

---

## 8. Verifying System Health (Smoke Test Sequence)

1. `GET /actuator/health` → status: UP
2. `POST /api/v1/auth/login` → valid JWT
3. `GET /api/v1/training-programs` → list returns
4. `GET /api/v1/admin/training/reports/overview` → admin metrics
5. `GET /api/v1/admin/training/reports/exceptions` → exception queue (may be empty)
6. `GET /api/v1/admin/training/reports/audit` → audit log (may be empty)

---

## 9. Scheduled Jobs

| Job | Schedule | Purpose |
|-----|----------|---------|
| Training Reminder Scheduler | Configurable | Send reminder notifications before training starts |

### Monitoring scheduled jobs
```sql
-- Check last notification sent
SELECT MAX(created_at) FROM notifications WHERE type LIKE 'TRAINING%';
```

---

## 10. Key Configuration Variables

| Variable | Purpose | Default |
|----------|---------|---------|
| `TRAINING_CANCELLATION_ADMIN_DAYS` | Admin cancellation window (days) | 7 |
| `TRAINING_CANCELLATION_TRAINEE_DAYS` | Trainee cancellation window (days) | 2 |
| `SPRING_PROFILES_ACTIVE` | Active profile (`dev`/`prod`) | dev |
| `CORS_ALLOWED_ORIGINS` | Allowed CORS origins | localhost only |
| `PAYMENT_PROVIDER_KEY` | Payment provider public key | mock |
| `PAYMENT_PROVIDER_SECRET` | Payment provider secret | mock |
| `PAYMENT_WEBHOOK_SECRET` | Payment webhook HMAC secret | mock |

> **WARNING**: In production, all secrets MUST be set via environment variables or a secret manager. Never use the default values.

---

## 11. Export Reports

### CSV Enrollment Export
```
GET /api/v1/admin/training/reports/export/enrollments
Authorization: Bearer <admin-token>
```

### CSV Audit Log Export
```
GET /api/v1/admin/training/reports/export/audit
Authorization: Bearer <admin-token>
```

---

## 12. Emergency Contacts & Escalation

| Severity | Condition | Action |
|----------|-----------|--------|
| P1 | Database down, no enrollments possible | Engineering on-call immediately |
| P2 | Payment provider unreachable | Check provider status page; notify engineering |
| P2 | Notification provider failing | Admin retry via exception queue; notify engineering |
| P3 | Certificate generation failing | Admin retry via `/controls/retry-certificate/{id}` |
| P3 | Capacity inconsistency detected | Do NOT manually update; escalate to engineering |
