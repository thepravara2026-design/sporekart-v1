# SPOREKART v3.0 — SPRINT 5B COMPLETION REPORT
## API SECURITY & ABUSE PROTECTION

**Sprint Status:** COMPLETE  
**Execution Date:** August 15, 2026  
**Git Branch:** `feature/sprint-5b-api-security-abuse-protection`  
**Test Suite:** 274/274 Passed (100% pass rate, 0 failures, 0 errors)  
**Flyway Migration:** `V18__api_security_idempotency_domain.sql`  

---

## 1. Executive Summary

Sprint 5B has established enterprise-grade, comprehensive API security and abuse protection across the full REST and event-driven surface of Sporekart v3.0 without regressing any pre-existing commerce capabilities (Catalog, Cart, Customer, Order, Payment, Shipping, Refund/Return, Admin).

All API endpoints are now guarded by multi-tiered, categorized rate limiters, strict payload size constraints, enforced `application/json` Content-Type controls, input query sanitization (preventing SQL wildcard Denial of Service and search overloads), strict pagination/sorting bounds, and database-backed idempotency replay control with webhook event deduplication.

---

## 2. Core Security & Abuse Protections Delivered

### 2.1 Categorized & Dimensioned Rate Limiting (`RateLimitingFilter.java`)
- **Filter Precedence:** Registered at `@Order(1)` ahead of request processing and authentication filters.
- **Categorization Risk Tiers:**
  - **AUTHENTICATION:** 10 requests / min (per IP / User)
  - **CHECKOUT_PAYMENT:** 20 requests / min
  - **SEARCH:** 30 requests / min
  - **ADMIN:** 60 requests / min
  - **WEBHOOK:** 200 requests / min
  - **GENERAL_MUTATION:** 60 requests / min
  - **GENERAL_READ:** 120 requests / min
- **Dimensional Keying:** Dynamically keys limits by `user:<userId>:<category>` for authenticated requests and `ip:<clientIp>:<category>` for anonymous traffic (resolving `X-Forwarded-For` proxy headers securely).
- **HTTP 429 Error Envelope & Headers:** Returns standardized JSON error response (`"code": "RATE_LIMIT_EXCEEDED"`, HTTP status 429) accompanied by `Retry-After: 60`, `X-RateLimit-Limit`, and `X-RateLimit-Remaining` headers.

### 2.2 Request Payload & Header Controls (`RequestProtectionFilter.java`)
- **Filter Precedence:** Registered at `@Order(2)`.
- **Payload Max Limit:** Enforces a strict 2MB (2,097,152 bytes) payload boundary on all HTTP requests. Exceeding requests are rejected immediately with HTTP 413 (`"code": "PAYLOAD_TOO_LARGE"`).
- **Content-Type Enforcement:** Enforces `application/json` Content-Type on all mutating HTTP methods (`POST`, `PUT`, `PATCH`). Non-conforming content types return HTTP 415 (`"code": "UNSUPPORTED_MEDIA_TYPE"`).

### 2.3 Input Hardening & Search Protection (`ProductSearchCriteria.java`, `CategoryApplicationService.java`)
- **Search Query Length:** Capped at 100 characters maximum length. Query inputs exceeding 100 characters return HTTP 400 (`"code": "INVALID_SEARCH_QUERY"`).
- **Wildcard DoS Sanitization:** Strips SQL/Regex wildcard overload characters (`%`, `*`, `?`, `_`). Pure wildcard overload queries (e.g. `%%%%%%`) are safely rejected (`"code": "INVALID_SEARCH_QUERY"`).
- **Pagination & Sorting Bounds:** Capped page size at max 100 items per page (`"code": "CATALOG_INVALID_PAGE_SIZE"`). Enforced strict sort field whitelists (`"code": "CATALOG_INVALID_SORT"`).

### 2.4 Idempotency & Webhook Deduplication Subsystem (`IdempotencyService.java`, `V18 Migration`)
- **Database Schema:** `V18__api_security_idempotency_domain.sql` creates `idempotency_records` table with unique constraint `uq_idempotency_actor_key`.
- **Idempotency Lifecycle:**
  - Validates `Idempotency-Key` format and length (max 128 characters).
  - Hashes request payloads using SHA-256 to guarantee request payload consistency across retries.
  - Replays original response code and body deterministically for completed idempotent requests.
  - Rejects conflicting payloads reusing an active key with HTTP 400 (`"code": "INVALID_IDEMPOTENCY_KEY"`).
- **Webhook Deduplication:** Integrated deduplication for Razorpay and provider-agnostic shipping webhooks via `findByProviderAndProviderEventId` to prevent duplicate ledger transactions and shipment status transitions.

---

## 3. Test Verification & Suite Summary

- **Total Tests:** 274
- **Passed:** 274 (100%)
- **Failures:** 0
- **Errors:** 0
- **New Integration Test Suites:**
  1. `ApiRateLimitingIntegrationTest.java` (Auth endpoint rate limit breach, 429 response, `Retry-After: 60` header, rate limit response envelope).
  2. `ApiPayloadAndValidationIntegrationTest.java` (415 unsupported content type, 400 excessive page size, 400 invalid sort field, 400 wildcard search overload, 400 search query length limit).
  3. `ApiIdempotencyAndConcurrencyIntegrationTest.java` (Idempotency key format validation, SHA-256 payload mismatch detection, deterministic payment webhook deduplication).

---

## 4. Master Index Update & File Checklist

- `V18__api_security_idempotency_domain.sql`: Schema migration for idempotency records.
- `RateLimitingFilter.java`: Multi-tier, category-aware rate limiter (`@Order(1)`).
- `RequestProtectionFilter.java`: 2MB payload cap and `application/json` Content-Type filter (`@Order(2)`).
- `IdempotencyService.java` & `IdempotencyRecordRepository.java`: Database-backed idempotency manager.
- `GlobalExceptionHandler.java`: Updated exception handlers for 429, 413, 415, 400, and search validation errors.
- `ProductSearchCriteria.java` & `CategoryApplicationService.java`: Input length, wildcard sanitization, and pagination bounds.
- `application-test.yml`: Configured test-environment rate limits and payload caps.
- `docs/sprint-5b-api-security-abuse-protection.md`: Comprehensive completion report.
- `docs/README.md`: Updated with Section 10 documentation index.
