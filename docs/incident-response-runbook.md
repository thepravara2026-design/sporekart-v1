# SPOREKART v3.0 — INCIDENT RESPONSE RUNBOOK

## 1. INCIDENT RESPONSE WORKFLOW

For all production incidents, follow the standard 6-stage lifecycle:

```text
DETECT ──> CONTAIN ──> DIAGNOSE ──> RECOVER ──> VERIFY ──> DOCUMENT
```

---

## 2. RUNBOOK 1: PAYMENT PROVIDER OUTAGE OR WEBHOOK BACKLOG

### Detection
- Metric alert: `sporekart_payment_attempts_total` experiencing high failures or `PENDING_RECONCILIATION` spikes.
- Webhook endpoint `/api/v1/payments/webhooks/razorpay` returning HTTP 5xx or timing out.

### Containment & Diagnosis
1. Verify Razorpay / payment provider status dashboard.
2. Inspect log files for `PaymentReconciliationService` and `RazorpayPaymentProviderImpl`:
   ```bash
   docker logs sporekart-backend | grep -i "payment"
   ```

### Recovery & Mitigation
1. **Do NOT disable idempotency keys or payment reconciliation**.
2. If Razorpay is degraded, pending transactions will transition to `PENDING_RECONCILIATION`.
3. Once provider connectivity is restored, trigger background reconciliation:
   ```bash
   curl -X POST -H "Authorization: Bearer <ADMIN_JWT>" http://localhost:8080/api/v1/admin/payments/reconcile
   ```
4. Verify payment status updates to `SUCCESS` or `FAILED` without duplicate order fulfillment.

---

## 3. RUNBOOK 2: DATABASE CONNECTION POOL EXHAUSTION

### Detection
- Log message: `HikariPool-1 - Connection is not available, request timed out after 30000ms`.
- Actuator health check `/actuator/health` reporting `DOWN` for `db`.

### Containment & Diagnosis
1. Check active database connections in PostgreSQL:
   ```sql
   SELECT count(*), state, query FROM pg_stat_activity GROUP BY state, query;
   ```
2. Identify long-running queries or uncommitted transactions.

### Recovery
1. Restart backend service to force Hikari connection pool reset:
   ```bash
   docker-compose -f docker-compose.prod.yml restart backend
   ```
2. If connection demand exceeds capacity, verify DB capacity calculation:
   $$\text{Max DB Connections} \ge \text{Instances} \times \text{DB\_POOL\_SIZE}$$
3. Adjust `DB_POOL_SIZE` in `.env` if database host supports higher connections.

---

## 4. RUNBOOK 3: RATE LIMITING & ABUSE ATTACK

### Detection
- Spike in HTTP 429 Too Many Requests responses.
- High traffic from specific client IP ranges targeting `/api/v1/auth/login` or checkout endpoints.

### Mitigation
1. Verify `RateLimitingFilter` is actively blocking traffic and returning `HTTP 429` with `Retry-After: 60`.
2. Inspect correlated client IP in logs:
   ```bash
   docker logs sporekart-backend | grep "Rate limit breached"
   ```
3. Block offending client IP ranges at reverse proxy (Nginx) level:
   ```nginx
   deny 192.0.2.1;
   ```
