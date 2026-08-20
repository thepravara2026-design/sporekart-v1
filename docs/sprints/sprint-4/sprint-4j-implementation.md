# SPOREKART v3.0 — Sprint 4J Implementation Report

**Branch**: feature/sprint-4j-platform-hardening
**Date**: 2026-08-15
**Sprint Goal**: Platform Hardening, Security, Observability, Performance, Resilience, Production Readiness

---

## 1. Changes Implemented

### Security (P1)

#### SecurityConfig.java — Tightened Authentication & Authorization
- Removed `permitAll()` for `/api/v1/orders/**`, `/api/v1/returns/**`, `/api/v1/customer/**`, `/api/v1/admin/**`
- All business endpoints now require authentication via `anyRequest().authenticated()`
- Admin routes (`/api/v1/admin/**`) require `ROLE_ADMIN` at the filter-chain level (defence-in-depth)
- Added `@EnableMethodSecurity` to support `@PreAuthorize` on controllers
- Added security response headers:
  - `Content-Security-Policy: default-src 'self'; frame-ancestors 'self'; object-src 'none'`
  - `Referrer-Policy: strict-origin-when-cross-origin`
  - `X-Content-Type-Options: nosniff`
  - `Strict-Transport-Security: max-age=31536000; includeSubDomains`
  - `X-Frame-Options: SAMEORIGIN`

#### ReturnController.java — IDOR Fix
- Replaced `@RequestHeader("X-Customer-Id", defaultValue="cust-101")` with `Authentication` principal
- All 5 endpoints (eligibility check, create return, get return, list returns, cancel return) now use `resolveCustomerId(authentication)`
- Hardcoded test fixture `cust-101` default eliminated

#### CustomerSupportController.java — IDOR Fix
- Replaced all 6 `X-Customer-Id` header injections with `Authentication` principal
- Endpoints: createTicket, listTickets, getTicket, addMessage, reopenTicket, requestReplacement

#### CustomerReviewController.java — IDOR Fix
- Replaced `X-Customer-Id` injection in `submitReview` and `voteHelpfulness`
- Public endpoints (listProductReviews, getRatingSummary) remain unauthenticated as intended

#### RateLimitingFilter.java — NEW
- Per-IP sliding window rate limiting (60 RPM default; configurable via `RATE_LIMIT_RPM`)
- 429 Too Many Requests response with `X-RateLimit-Limit`, `X-RateLimit-Remaining`, `Retry-After` headers
- Exempt: GET catalog browsing, actuator, OpenAPI docs, webhooks
- Enabled/disabled via `RATE_LIMIT_ENABLED` env var
- In-memory (single-instance); Redis migration deferred per DEBT-002

### Database (P2)

#### V16__platform_hardening_indexes_and_constraints.sql — NEW
New Flyway migration adding:
- `idx_returns_customer_status` — composite index on `returns(customer_id, status)`
- `idx_returns_customer_created` — composite index on `returns(customer_id, created_at DESC)`
- `idx_refund_records_customer_status` — composite index on `refund_records(customer_id, status)`
- `chk_refund_records_amount_positive` — CHECK constraint on `refund_records.amount > 0`
- `idx_support_tickets_customer_status` — composite index on `support_tickets(customer_id, status)`
- `idx_support_tickets_created_at` — index on `support_tickets(created_at DESC)`
- `idx_product_reviews_product_status` — composite index on `product_reviews(product_id, status)`
- `idx_product_reviews_customer_status` — composite index on `product_reviews(customer_id, status)`
- `idx_payment_webhook_events_provider_event` — idempotency lookup index on webhook events
- `idx_orders_customer_created` — composite index on `orders(customer_id, created_at DESC)`
- `idx_order_items_product_id` — index on `order_items(product_id)`
- `idx_shipments_provider_status` — composite index on `shipments(provider, status)`
- `idx_inventory_sku_status` — composite index on `inventory_items(sku, low_stock_threshold)`

### Configuration (P2)

#### application.yml
- Added `server.tomcat.connection-timeout: 20000ms`
- Added `server.tomcat.threads.max: 200` / `min-spare: 10`
- Added HikariCP defaults: `maximum-pool-size: 10`, `minimum-idle: 5`, `connection-timeout: 30s`, `idle-timeout: 600s`, `max-lifetime: 1800s`
- Added `app.rate-limit.enabled` and `app.rate-limit.requests-per-minute` configuration keys
- Enhanced MDC logging pattern to include `%X{userId:-}` alongside existing `%X{requestId:-}`
- Added `org.springframework.security: WARN` log level

#### application-prod.yml
- Added production HikariCP config: `maximum-pool-size: 20`, `leak-detection-threshold: 60s`
- Added production thread pool: `max: 400`, `min-spare: 20`
- Added `app.rate-limit.enabled: true` with `requests-per-minute: 120` for production

---

## 2. Documentation Produced

| Document | Path |
| :--- | :--- |
| Reconnaissance Report | `docs/sprint-4j-reconnaissance.md` |
| Threat Model (STRIDE) | `docs/sprint-4j-threat-model.md` |
| Production Readiness Scorecard | `docs/sprint-4j-production-scorecard.md` |
| Failure Matrix | `docs/sprint-4j-failure-matrix.md` |
| Rate Limiting Runbook | `docs/runbooks/rate-limiting-runbook.md` |
| Completion Report (this file) | `docs/sprint-4j-completion-report.md` |
| Implementation Report | `docs/sprint-4j-implementation.md` |

---

## 3. Authorization Matrix Update

| Endpoint | Pre-4J Auth | Post-4J Auth |
| :--- | :--- | :--- |
| `POST /api/v1/orders` | ❌ permitAll | ✅ authenticated |
| `GET /api/v1/orders/{id}` | ❌ permitAll | ✅ authenticated + IDOR check |
| `POST /api/v1/orders/{ref}/returns` | ❌ permitAll (header spoofable) | ✅ authenticated principal |
| `GET /api/v1/customer/returns` | ❌ permitAll (header spoofable) | ✅ authenticated principal |
| `POST /api/v1/customer/support/tickets` | ❌ permitAll (header spoofable) | ✅ authenticated principal |
| `POST /api/v1/reviews` | ❌ permitAll (header spoofable) | ✅ authenticated principal |
| `POST /api/v1/reviews/{ref}/vote` | ❌ permitAll (header spoofable) | ✅ authenticated principal |
| `GET /api/v1/admin/**` | ❌ permitAll | ✅ ROLE_ADMIN required |

---

## 4. Technical Debt Updated

No new technical debt items introduced. Existing DEBT-001, DEBT-002, DEBT-003 remain deferred as documented.

---

## 5. Constraints Respected

- No new business features introduced
- No domain model changes
- No breaking API changes to existing endpoints
- All existing tests pass unchanged
- V16 migration is fully idempotent (IF NOT EXISTS / DO $$ blocks)