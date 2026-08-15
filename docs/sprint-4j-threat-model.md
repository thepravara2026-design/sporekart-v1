# SPOREKART v3.0 — Sprint 4J Threat Model

**Branch**: feature/sprint-4j-platform-hardening
**Date**: 2026-08-15
**Standard**: STRIDE (Spoofing, Tampering, Repudiation, Information Disclosure, Denial of Service, Elevation of Privilege)

---

## Threat Matrix

| ID | Threat | Category | Asset | Attack Vector | Likelihood | Impact | Mitigation | Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| T-01 | Customer identity spoofing via X-Customer-Id header | Spoofing | All customer endpoints (Returns, Support, Reviews) | HTTP header injection | HIGH (pre-fix) | CRITICAL | Replaced header trust with Spring Authentication principal | **MITIGATED** |
| T-02 | Unauthenticated admin API access | Elevation of Privilege | /api/v1/admin/** | No authentication required | HIGH (pre-fix) | CRITICAL | SecurityConfig: hasRole(ADMIN) enforcement + @PreAuthorize | **MITIGATED** |
| T-03 | Payment webhook replay attack | Tampering | /api/v1/payments/webhooks/razorpay | Re-delivering old signed payloads | MEDIUM | HIGH | Idempotency key in payment_webhook_events + processing_status check | **EXISTING CONTROL** |
| T-04 | Razorpay webhook signature bypass | Spoofing | Payment processing pipeline | Crafting unsigned webhook request | MEDIUM | CRITICAL | HMAC-SHA256 verification in PaymentApplicationService | **EXISTING CONTROL** |
| T-05 | Denial of Service via API flooding | DoS | All mutation endpoints | High-frequency POST requests from one IP | HIGH | HIGH | Per-IP RateLimitingFilter (60 RPM default, 429 response) | **MITIGATED** |
| T-06 | SQL Injection via catalog filter parameters | Tampering | Product catalog search | Malicious sort/filter parameter injection | LOW | HIGH | Spring Data JPA parameterized queries throughout; no native SQL in filter paths | **EXISTING CONTROL** |
| T-07 | Cross-Site Scripting (reflected) | Information Disclosure | Any JSON API response | Injecting script via request parameter | LOW | MEDIUM | include-stacktrace:never, include-message:never; Content-Security-Policy header | **MITIGATED** |
| T-08 | Clickjacking via iframe embedding | Tampering | Frontend UI | iframe embedding of Sporekart UI | LOW | MEDIUM | X-Frame-Options: SAMEORIGIN header | **EXISTING + MITIGATED** |
| T-09 | Content type sniffing attack | Tampering | API responses | Browser MIME sniffing override | LOW | LOW | X-Content-Type-Options: nosniff header | **MITIGATED** |
| T-10 | HTTP to HTTPS downgrade attack | Information Disclosure | Production HTTPS traffic | Network-level MITM | LOW | HIGH | HSTS: max-age=31536000; includeSubDomains | **MITIGATED** |
| T-11 | Database connection exhaustion | DoS | PostgreSQL | Excessive concurrent connections | MEDIUM | HIGH | HikariCP pool: max=20 prod, connection-timeout=30s, leak-detection=60s | **MITIGATED** |
| T-12 | Thread pool exhaustion via slow requests | DoS | Tomcat thread pool | Slow HTTP clients holding threads | MEDIUM | HIGH | connection-timeout: 20s; max threads: 400 prod | **MITIGATED** |
| T-13 | IDOR on return/support/review resources | Spoofing | Customer-specific data | Passing another customer ID in path | HIGH (pre-fix) | HIGH | Ownership check in service layer + Authentication principal enforcement | **MITIGATED** |
| T-14 | Sensitive data in error responses | Information Disclosure | Error payloads | Triggering exceptions to read stack traces | LOW | MEDIUM | include-stacktrace:never; GlobalExceptionHandler sanitizes all responses | **EXISTING CONTROL** |
| T-15 | Secret leakage via committed .env | Information Disclosure | Credentials | Git history scanning | LOW | CRITICAL | .env in .gitignore; DATABASE_PASSWORD required env var (no default) | **EXISTING CONTROL** |
| T-16 | Insecure H2 console access in dev | Elevation of Privilege | H2 in-memory database | Local developer workstation access | LOW (dev only) | MEDIUM | H2 console disabled in prod profile; frame-options: sameOrigin in dev | **CONTROLLED** |
| T-17 | Refund amount tampering at DB level | Tampering | refund_records.amount | Direct DB write with negative amount | LOW | HIGH | CHECK constraint chk_refund_records_amount_positive (V16 migration) | **MITIGATED** |
| T-18 | Review identity fraud (X-Customer-Id) | Spoofing | Product reviews | Submitting review as another customer | HIGH (pre-fix) | MEDIUM | CustomerReviewController: Authentication principal extraction | **MITIGATED** |

---

## Residual Risk

| Threat | Residual Risk | Rationale |
| :--- | :--- | :--- |
| T-03 (webhook replay) | LOW | Idempotency key enforced; replay returns idempotent response |
| T-05 (DoS via flooding) | LOW-MEDIUM | In-memory rate limiter; multi-instance deployments need Redis (DEBT-002) |
| T-06 (SQLi) | VERY LOW | JPA parameterized queries throughout; no raw SQL in customer-facing paths |
| T-16 (H2 console) | LOW | Dev-only; production profile disables H2 entirely |

---

## Out of Scope (Deferred)

- Distributed rate limiting via Redis cluster (DEBT-002)
- JWT/OAuth2 token-based authentication (current system is principal-name based)
- WAF (Web Application Firewall) — infrastructure-level concern
- Penetration testing — requires staging environment
