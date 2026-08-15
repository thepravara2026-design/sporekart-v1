# SPOREKART v3.0 — Sprint 4J Failure Matrix

**Date**: 2026-08-15
**Sprint**: 4J Platform Hardening

---

## Security Failure Scenarios

| ID | Component | Failure Scenario | Expected Behavior | Actual Behavior (Post-Fix) | Test |
| :--- | :--- | :--- | :--- | :--- | :--- |
| SF-01 | SecurityConfig | Unauthenticated POST to /api/v1/orders | 401 Unauthorized | 401 Unauthorized JSON with UNAUTHORIZED code | SecurityConfigTest |
| SF-02 | SecurityConfig | Unauthenticated GET to /api/v1/admin/orders | 401 Unauthorized | 401 Unauthorized | SecurityConfigTest |
| SF-03 | SecurityConfig | Authenticated non-admin GET to /api/v1/admin/orders | 403 Forbidden | 403 Forbidden with FORBIDDEN code | SecurityConfigTest |
| SF-04 | ReturnController | POST /api/v1/orders/{ref}/returns without auth | 401 Unauthorized | 401 Unauthorized | ReturnControllerTest |
| SF-05 | CustomerSupportController | POST /api/v1/customer/support/tickets without auth | 401 Unauthorized | 401 Unauthorized | SupportTicketLifecycleIntegrationTest |
| SF-06 | CustomerReviewController | POST /api/v1/reviews without auth | 401 Unauthorized | 401 Unauthorized | ProductReviewLifecycleIntegrationTest |
| SF-07 | RateLimitingFilter | 61st POST request in 60 seconds from same IP | 429 Too Many Requests | 429 with X-RateLimit headers and Retry-After:60 | Manual / new test |
| SF-08 | Payment Webhook | Webhook with invalid HMAC signature | 400 Bad Request or ignored | Service-layer HMAC rejection | PaymentWebhookSecurityTest |

---

## Resilience Failure Scenarios

| ID | Component | Failure Scenario | Expected Behavior | Actual Behavior | Recovery |
| :--- | :--- | :--- | :--- | :--- | :--- |
| RF-01 | HikariCP | DB connection pool exhausted | New requests get 503 or timeout error | ConnectionTimeout exception after 30s mapped to 503 by GlobalExceptionHandler | Pool auto-recovers as connections are returned |
| RF-02 | Tomcat | Slow client holding connection beyond 20s | Connection forcibly closed | Tomcat drops connection; 503 to client | Automatic; thread returned to pool |
| RF-03 | ReservationExpiryScheduler | Scheduler throws uncaught RuntimeException | Task continues on next cycle | @Scheduled silently swallows exception; next run proceeds | Logged as ERROR; task continues |
| RF-04 | ShipmentReconciliationScheduler | External provider unreachable | No state corruption | Service logs warning; marks stale shipments for retry | Next reconciliation cycle retries |
| RF-05 | Inventory optimistic lock | Concurrent stock modification | One transaction wins; other gets CONFLICT | ObjectOptimisticLockingFailureException -> 409 CART_CONCURRENCY_CONFLICT | Client retries |
| RF-06 | Payment idempotency | Duplicate payment creation request | Idempotent response (no double charge) | Existing payment record returned; no second provider call | Verified by PaymentRefundIdempotencyTest |

---

## Database Failure Scenarios

| ID | Component | Failure Scenario | Expected Behavior | Actual Behavior | Recovery |
| :--- | :--- | :--- | :--- | :--- | :--- |
| DF-01 | refund_records | INSERT with amount=0 or negative | DB rejects with constraint violation | CHECK constraint chk_refund_records_amount_positive raises error | Transaction rolls back; 400 returned |
| DF-02 | payment_webhook_events | Duplicate provider_event_id webhook | No duplicate processing | Idempotency check returns existing record; no reprocessing | Idempotent response returned |
| DF-03 | orders | Large order history query without index | Slow query / timeout | idx_orders_customer_created composite index used by planner | Fast response |
| DF-04 | Flyway migration V16 | Migration fails on existing constraint | Idempotent IF NOT EXISTS | No-op; migration reports success | Rerunnable |

---

## Security Header Verification

| Header | Expected Value | Set By |
| :--- | :--- | :--- |
| X-Content-Type-Options | nosniff | SecurityConfig.headers().contentTypeOptions() |
| X-Frame-Options | SAMEORIGIN | SecurityConfig.headers().frameOptions() |
| Strict-Transport-Security | max-age=31536000; includeSubDomains | SecurityConfig.headers().httpStrictTransportSecurity() |
| Content-Security-Policy | default-src 'self'; frame-ancestors 'self'; object-src 'none' | SecurityConfig.headers().contentSecurityPolicy() |
| Referrer-Policy | strict-origin-when-cross-origin | SecurityConfig.headers().referrerPolicy() |
| X-RateLimit-Limit | 60 (or configured value) | RateLimitingFilter |
| X-RateLimit-Remaining | n remaining | RateLimitingFilter |
