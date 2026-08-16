# SPOREKART v3.0 — Observability Operations Runbook

## Overview

This operational runbook provides diagnostic procedures, metrics references, Grafana dashboard descriptions, and emergency remediation steps for SPOREKART v3.0 production incidents triggered by Prometheus alert rules or operator observations.

---

## 1. Dashboard Reference

| Dashboard Name | Grafana UID | Primary Focus | Key Metrics Monitored |
|---|---|---|---|
| **Sporekart Application Overview** | `sporekart-app-overview` | Global API health & traffic patterns | `sporekart_http_requests_latency`, `http_server_requests_seconds`, CPU, RAM |
| **Sporekart Database & HikariCP** | `sporekart-database-hikaricp` | HikariCP pool saturation & DB connectivity | `hikaricp_connections_active`, `hikaricp_connections_pending`, timeouts |
| **Sporekart Payment Reliability** | `sporekart-payment-reliability` | Razorpay gateway calls & webhook status | `sporekart_payments_attempted`, `sporekart_provider_latency`, webhooks |
| **Sporekart Shipping Reliability** | `sporekart-shipping-reliability` | Shiprocket API latency & webhook tracking | `sporekart_shipments_created`, `sporekart_provider_errors`, webhooks |
| **Sporekart JVM Health** | `sporekart-jvm-health` | Memory pressure, GC pause duration, threads | `jvm_memory_used_bytes`, `jvm_gc_pause_seconds`, live threads |

---

## 2. Emergency Incident Runbooks

### Incident 1: High HTTP 5xx Error Rate (`HighHttp5xxErrorRate`)
- **Symptom:** Prometheus alert fires when > 5% of HTTP requests return 5xx status codes over 2 minutes.
- **Diagnostic Steps:**
  1. Open **Sporekart Application Overview** dashboard.
  2. Identify failing endpoint URI using `sporekart_http_errors{status=~"5.."}` tags.
  3. Search application logs for `requestId` / `correlationId` associated with 5xx HTTP responses.
- **Recovery Action:**
  - If error is caused by a failing database connection, verify PostgreSQL instance availability.
  - If error is caused by external provider downtime, verify provider status page and circuit breaker logs.

### Incident 2: Database Connection Pool Saturation (`HikariCpPoolSaturation` / `HikariCpConnectionAcquisitionTimeout`)
- **Symptom:** Active HikariCP connections exceed 85% of maximum pool size, or connection acquisition timeouts occur.
- **Diagnostic Steps:**
  1. Open **Sporekart Database & HikariCP** dashboard.
  2. Inspect `hikaricp_connections_pending` and `hikaricp_connections_active`.
  3. Search logs for slow request diagnostics `SLOW_REQUEST_DETECTED` to locate unindexed queries holding open connection locks.
- **Recovery Action:**
  - Dynamically scale up `DB_POOL_SIZE` environment variable if PostgreSQL instance capacity permits.
  - Terminate long-running idle transactions if necessary.

### Incident 3: Payment Provider Failure / Timeout Spike (`PaymentProviderFailureSpike` / `PaymentProviderTimeoutSpike`)
- **Symptom:** `sporekart_payments_failed_total` or `sporekart_provider_errors_total{type="timeout"}` rate spikes.
- **Diagnostic Steps:**
  1. Open **Sporekart Payment Reliability** dashboard.
  2. Check `sporekart_provider_latency_seconds` for Razorpay API calls.
  3. Verify whether failures are caused by network timeouts or Razorpay gateway 5xx responses.
- **Recovery Action:**
  - Verify Razorpay status page.
  - System automatically isolates payment provider timeouts without blocking checkout thread pools.

### Incident 4: Webhook Processing Failure Spike (`WebhookProcessingFailureSpike`)
- **Symptom:** `sporekart_webhooks_failed_total` rate exceeds 0.05 failures/sec over 2 minutes.
- **Diagnostic Steps:**
  1. Open **Sporekart Payment Reliability** or **Shipping Reliability** dashboard.
  2. Inspect `sporekart_webhooks_received_total` vs `sporekart_webhooks_duplicates_total` vs `sporekart_webhooks_failed_total`.
  3. Search logs for `WebhookProcessingStatus.FAILED` entries using the `provider_event_id` or `correlationId`.
- **Recovery Action:**
  - Deduplication prevents duplicate event replay. Re-trigger webhook reconciliation if provider callbacks were missed.

### Incident 5: JVM Memory Pressure (`JvmMemoryPressure`)
- **Symptom:** JVM heap usage exceeds 85% of maximum allocation for > 5 minutes.
- **Diagnostic Steps:**
  1. Open **Sporekart JVM Health** dashboard.
  2. Inspect heap usage trend vs GC pause times `rate(jvm_gc_pause_seconds_sum[1m])`.
- **Recovery Action:**
  - Capture heap dump via Actuator endpoint if authorized.
  - Restart container instance or increase JVM max heap allocation `-Xmx`.

---

## 3. Correlation & Diagnostic Tracing Procedure

Support and operations personnel can trace any production error using the `X-Correlation-ID` header returned in the HTTP response:

```bash
# Search logs by Correlation ID
grep "corr-test-12345" /var/log/sporekart/application.log
```

Logs contain structured MDC fields:
`[sporekart-backend,requestId,traceId,userId]` allowing end-to-end request correlation across HTTP filters, database operations, external provider calls, and background event listeners.
