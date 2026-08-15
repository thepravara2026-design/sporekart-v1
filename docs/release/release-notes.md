# SPOREKART v3.0.0 — Official Release Notes

**Release Date**: 2026-08-15
**Version**: v3.0.0
**Target Commit**: `2aeb2d0`

---

## Executive Overview

Sporekart v3.0.0 is a production-certified modular monolith e-commerce platform built on Spring Boot 3.4.2, Java 21, PostgreSQL 16, and React 18. This release delivers complete end-to-end commerce capabilities spanning catalog browsing, cart management, checkout calculation, order fulfillment state machines, payment gateway integration, inventory stock consistency, reverse logistics, customer support ticketing, product reviews, and platform hardening.

---

## Key Capabilities Delivered

### Core Commerce & Ordering
- **Product Catalog & Search**: Paginated product listing, category filtering, SKU availability checks, and product rating aggregation.
- **Cart & Checkout**: Persistent cart lifecycle, price calculation engine, promotional code support, and order generation.
- **Order Lifecycle Orchestration**: Deterministic state machine governing order transitions (`CREATED` → `PAID` → `PROCESSING` → `SHIPPED` → `DELIVERED`).

### Payments, Logistics & Support
- **Payment Gateway Orchestration**: Razorpay checkout integration, payment verification, HMAC-SHA256 webhook signature validation, and idempotent refund processing.
- **Shipping & Fulfillment**: Shiprocket integration, AWB generation, carrier tracking timeline, and shipment status reconciliation.
- **Returns & Reverse Logistics**: Customer return eligibility verification (14-day window), inspection logging, replacement request handling, and automated stock re-entry.
- **Customer Support & Disputes**: Ticket creation, SLA calculation, ticket message thread history, and agent assignment.
- **Product Reviews & Ratings**: Product review submission, moderation workflow, rating summaries, and helpfulness voting.

### Security, Observability & Hardening
- **Access Control & IDOR Fixes**: Complete migration from client header injection (`X-Customer-Id`) to server-side Spring `Authentication` principal extraction. Admin routes strictly protected with `ROLE_ADMIN`.
- **Security Response Headers**: Content-Security-Policy, HSTS, Referrer-Policy, X-Content-Type-Options, and X-Frame-Options configured.
- **Rate Limiting**: Sliding window per-IP rate limiter (60/120 RPM) protecting mutation endpoints against request floods.
- **Database Optimization**: Flyway V16 migration adding 12 composite performance indexes and `CHECK (amount > 0)` monetary constraints.
- **MDC Observability**: Structured log correlation passing `requestId` and `userId` across all log messages.

---

## Known Limitations & Technical Debt (Non-blocking)
- In-memory rate limiter bucket store (Redis distributed rate limiting deferred to multi-node scaling).
- Mock payment gateway active in development profile (`dev`), Razorpay production credentials required for live environment.