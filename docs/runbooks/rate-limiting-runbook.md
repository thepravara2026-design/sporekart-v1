# Runbook: Rate Limiting Operations

**Component**: RateLimitingFilter
**Owner**: Platform Engineering
**Updated**: 2026-08-15 (Sprint 4J)

---

## Overview

Sporekart uses a per-IP sliding window rate limiter applied to all mutation endpoints (POST, PUT, DELETE, PATCH).
Read-only catalog and health endpoints are excluded.

Default limits:
- **Development/QAT**: 60 requests per minute per IP
- **Production**: 120 requests per minute per IP (configurable via RATE_LIMIT_RPM env var)

---

## Symptoms: Rate Limit Engaged

1. Client receives `HTTP 429 Too Many Requests`
2. Response body: `{"error":"RATE_LIMIT_EXCEEDED","message":"Too many requests. Please retry after 60 seconds."}`
3. Response headers include:
   - `X-RateLimit-Limit: 60`
   - `X-RateLimit-Remaining: 0`
   - `Retry-After: 60`
4. Server logs: `WARN RateLimitingFilter - Rate limit exceeded for IP: {ip} on {method} {uri}, count: {n}`

---

## Diagnosis

### 1. Identify the IP being rate limited
```
grep "Rate limit exceeded" <log-file> | grep -oP 'IP: [0-9\.]+'
```

### 2. Check if IP is a legitimate proxy or load balancer
If Sporekart is behind a load balancer that sets `X-Forwarded-For`, the client IP is resolved from the first entry.
If the LB is NATing all requests to one IP, all clients share the same rate bucket.

**Fix**: Ensure the LB passes `X-Forwarded-For` with the real client IP.

### 3. Check current configuration
```
# Dev/QAT
grep "rate-limit" backend/src/main/resources/application.yml

# Prod
echo $RATE_LIMIT_RPM
```

---

## Remediation

### Temporarily raise the rate limit (prod)
```
export RATE_LIMIT_RPM=200
# Restart application
```

### Disable rate limiting (emergency only)
```
export RATE_LIMIT_ENABLED=false
# Restart application
```
**Warning**: Only disable rate limiting during a confirmed incident where rate limiting is causing false positives for legitimate traffic.

### Whitelist an IP (not currently supported)
The current in-memory implementation does not support IP allowlisting.
Deploy a WAF or reverse proxy (e.g., nginx) with IP allowlisting if needed.

---

## Scaling Considerations

The current rate limiter is **in-memory and single-instance**.
For multi-instance deployments, each instance maintains an independent bucket.
Effective rate per client = `RATE_LIMIT_RPM * instance_count`.

**Production action**: When scaling beyond 2 instances, migrate to Redis-backed rate limiting (see DEBT-002 in docs/TECHNICAL_DEBT.md).

---

## Escalation Path

1. Platform Engineering on-call
2. If blocking legitimate payment webhooks: immediately set `RATE_LIMIT_ENABLED=false` and create P0 incident
3. Payment webhook endpoint (/api/v1/payments/webhooks/**) is excluded from rate limiting
