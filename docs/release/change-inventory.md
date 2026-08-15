# SPOREKART v3.0 — Release Change Inventory

**Release Version**: v3.0.0
**Target Commit**: `2aeb2d0`
**Date**: 2026-08-15

---

## 1. Change Log Inventory Across All Sprints

| Sprint | Category | Key Capability / Change | Primary Module(s) | Risk | Validation |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Sprint 1** | Foundation | Modular monolith structure, PostgreSQL Flyway migrations (V1-V5), Catalog & Customer APIs | `catalog`, `customer` | Low | Unit & DB tests |
| **Sprint 2** | Cart & Pricing | Persistent cart, item pricing, tax calculation engine | `cart`, `checkout` | Low | Price calculation tests |
| **Sprint 3A-3E** | Core E-Commerce | Checkout orchestration, order creation, inventory reservation, payment gateway integration | `checkout`, `order`, `inventory`, `payment` | High | Payment & reservation tests |
| **Sprint 3F-3J** | Order & Hardening | Order lifecycle state machine, shipping integration, return/refund, platform hardening baseline | `order`, `shipment`, `returns`, `refund` | High | State machine & concurrency tests |
| **Sprint 4A-4D** | Lifecycle Refinement | Generic transition APIs, Razorpay webhook signature verification, Shiprocket handoff, reverse logistics | `order`, `payment`, `shipment`, `returns` | High | Webhook security & handoff tests |
| **Sprint 4E-4I** | Support & Reviews | Inventory stock consistency, shipping reconciliation, customer support tickets, product reviews & ratings | `inventory`, `shipment`, `support`, `review` | Medium | Lifecycle & moderation tests |
| **Sprint 4J** | Hardening | IDOR fix (`Authentication` principal extraction), `SecurityConfig` tightening, `RateLimitingFilter`, HikariCP tuning, Flyway V16 indexes | Platform Core | Critical | Full 256 test suite |
| **Sprint 4K** | Release & Go-Live | Release candidate creation, complete documentation suite, operational runbooks, go-live checklists | Documentation & Release | Low | Release audit & certification |

---

## 2. File Change Footprint (Sprint 4J/4K Release Package)

```
backend/src/main/java/com/sporekart/application/configuration/SecurityConfig.java
backend/src/main/java/com/sporekart/application/web/RateLimitingFilter.java
backend/src/main/java/com/sporekart/modules/returns/controller/ReturnController.java
backend/src/main/java/com/sporekart/modules/support/controller/CustomerSupportController.java
backend/src/main/java/com/sporekart/modules/review/controller/CustomerReviewController.java
backend/src/main/java/com/sporekart/modules/shipment/controller/ShipmentController.java
backend/src/main/resources/application.yml
backend/src/main/resources/application-prod.yml
backend/src/main/resources/db/migration/V16__platform_hardening_indexes_and_constraints.sql
docs/* (Comprehensive Operational & Release Certification Suite)
```