# SPOREKART v3.0 — Sprint 4J: Platform Hardening Reconnaissance Report

**Branch**: `feature/sprint-4j-platform-hardening`
**Date**: 2026-08-15
**Sprint Goal**: Security, Observability, Resilience, Performance, Production Readiness (no new business features)

---

## 1. Repository State at Reconnaissance

| Dimension | Finding |
| :--- | :--- |
| Last Commit | `5c9a505` Sprint 4I Reviews complete |
| Modules | catalog, cart, checkout, order, payment, inventory, shipment, returns, support, review, notification, customer |
| Framework | Spring Boot 3.4.2, Java 21, Spring Security (stateless) |
| Auth Model | `X-Customer-Id` header on 4G-4I domains; Spring `Authentication` on order/payment |
| Tests | 50+ test classes across domain, integration, concurrency suites |
| Migrations | V1-V15 applied |
| Rate Limiting | MISSING |
| Security Headers | Partial - frameOptions(sameOrigin) only |
| Actuator Exposure | health, info only (correct) |

---

## 2. Critical Security Findings (P1)

### SEV-1: Unauthenticated Header Trust / IDOR Vulnerability
Affected Controllers: ReturnController, CustomerSupportController, CustomerReviewController, CustomerInventoryController
All use `@RequestHeader("X-Customer-Id", defaultValue="cust-101")` — any caller can spoof any customer.
Fix: Replace with `Authentication` principal extraction.

### SEV-2: Over-Permissive SecurityConfig permitAll Block
The following are ALL permitting unauthenticated access:
- `/api/v1/orders/**`
- `/api/v1/returns/**`
- `/api/v1/customer/**`
- `/api/v1/admin/**`
Fix: Tighten SecurityConfig to require authentication on all business endpoints; require ROLE_ADMIN for admin.

### SEV-3: Missing Rate Limiting
No rate limiting on any endpoint. DoS and enumeration risk.
Fix: Add per-IP sliding window RateLimitingFilter.

### SEV-4: Missing Security Response Headers
Missing: X-Content-Type-Options, HSTS, X-XSS-Protection, Referrer-Policy, Content-Security-Policy.
Fix: Add via SecurityConfig headers customizer.

---

## 3. Observability Gaps (P2)

- OBS-1: No correlation ID in scheduled tasks / domain events
- OBS-2: JSON logging not configured; plain text log format
- OBS-3: No HTTP access log / request audit log
- OBS-4: No custom business metrics via Micrometer

---

## 4. Resilience Gaps (P2)

- RES-1: No request timeout configuration on Tomcat
- RES-2: Scheduled tasks have no error boundary / alerting
- RES-3: No HikariCP pool tuning

---

## 5. Database Hardening Gaps (P2)

- DB-1: Missing CHECK constraints on monetary columns (payments.amount, refund_records.refund_amount)
- DB-2: Missing indexes on returns(customer_id, status), support_tickets(customer_id, status), product_reviews(product_id, status), payment_webhook_events(provider_event_id)
- DB-3: No updated_at on several audit-critical tables

---

## 6. Sprint 4J Implementation Plan

### P1 - Security Critical
1. Tighten SecurityConfig - remove permitAll for admin/orders/returns/customer
2. Replace X-Customer-Id header trust with Authentication principal in all 4G-4I controllers
3. Add security response headers filter
4. Add per-IP rate limiting filter

### P2 - Resilience and Observability
5. Add HikariCP pool tuning and server timeouts
6. Add V16 database migration for missing indexes and CHECK constraints
7. Add structured logging configuration

### P3 - Documentation
8. Sprint 4J threat model, production scorecard, failure matrix, runbooks, completion report
