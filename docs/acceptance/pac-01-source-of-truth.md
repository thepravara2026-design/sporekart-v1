# SPOREKART v3.0 — PAC-01 Source of Truth & Governance Alignment

**Document ID:** `PAC-01-SOURCE-OF-TRUTH`  
**Sprint:** `PAC-01 — Application Readiness & Architecture Acceptance`  
**Author:** Antigravity FAANG Master Execution Agent  
**Date:** 2026-08-19  
**Status:** Certified Authoritative Baseline  

---

## 1. Executive Summary

This document establishes the single source of truth for SPOREKART v3.0 architecture, implementation roadmaps, completed sprint tracks, and governance boundaries prior to final end-to-end Production Acceptance Certification (PAC-01).

---

## 2. Authoritative Architecture & Frameworks

### 2.1 Core Architectural Pattern
- **Pattern:** Modular Monolith with bounded contexts, strict domain encapsulation, and transactional outbox for asynchronous messaging.
- **Backend Stack:** Java 21 LTS, Spring Boot 3.4.2, Maven, Spring Data JPA, Spring Security (JWT / Role Guards), Flyway DB Migrations, PostgreSQL / H2 in-memory DB.
- **Frontend Stack:** React 18+, Vite 5.4+, TypeScript 5.x, TanStack Query v5, Axios, Vitest, React Testing Library.
- **Infrastructure:** Docker, Docker Compose (`docker-compose.yml`, `docker-compose.prod.yml`), GitHub Actions.

### 2.2 Authoritative Domain & Module Boundaries
1. **AUTH & IDENTITY:** JWT Authentication, Role-based Access Control (`ROLE_CUSTOMER`, `ROLE_GROWER`, `ROLE_TRAINEE`, `ROLE_SELLER`, `ROLE_ADMIN`), Password Hashing (BCrypt), Security Auditing.
2. **CATALOG:** Products, Categories, Search/Filters, Stock status, Pricing.
3. **CART:** Session & Persistent Customer Carts, Line Item Management.
4. **CHECKOUT:** Pricing Engine, Multi-Step Checkout, Shipping Address, Payment Method Selection, Order Creation Handoff.
5. **ORDER:** Order Lifecycle State Machine (`CREATED` → `PAID` → `CONFIRMED` → `FULFILLED` → `SHIPPED` → `DELIVERED` / `CANCELLED` / `REFUNDED`), Order References.
6. **INVENTORY:** Stock Reservation, Concurrency Control, On-hand / Reserved Stock, Damaged Stock, Grower SKU boundaries.
7. **PAYMENT:** Provider Abstraction (`PaymentProvider`), Mock Gateway implementation, Refund Abstraction (`RefundService`), Idempotency.
8. **SHIPMENT:** Shipping Provider Abstraction (`ShippingProvider`), Shiprocket Integration foundation, Tracking, Manifest generation.
9. **NOTIFICATION & OUTBOX:** Transactional Outbox Pattern (`OutboxEvent`), Notification Dispatcher, Multi-channel (Email, In-App, SMS), Retry & Dead-lettering.
10. **GROWER (GB-01 → GB-03):** Multi-tenant Grower Portal (`grower_id`), Product Management, Stock & Inventory Controls, Orders, Shipments, Reports, Profile & Settings.
11. **TRAINING (Sprint 7G):** Training Programs, Batches, Seat Allocation, Capacity Policies, Trainee Enrollment, Cancellation & Refund Rules, Operations & Reports.
12. **SELLER (FD-15):** Seller Marketplace Foundation, Dashboard, Product Management, Inventory, Order Fulfillment.
13. **ADMIN (FD-16):** Master Operations Dashboard, Returns Management, Training Console, Notification Operations, User Governance.

---

## 3. Discovered Authoritative Roadmaps & Completed Tracks

| Track / Sprint ID | Description | Scope & Verification | Status |
|-------------------|-------------|----------------------|--------|
| **Sprint 7A → 7X** | Master Backend & Modular Architecture Roadmap | Modular boundaries, Outbox pattern, Security, Performance | **AUTHORITATIVE & COMPLETE** |
| **GB-01 → GB-03** | Grower Backend & Production Readiness | Flyway V42 migration, Tenant isolation (`grower_id`), Audit logging | **AUTHORITATIVE & COMPLETE** |
| **FD-01 → FD-25** | Complete Frontend Implementation Track | 425 Vitest tests, TypeScript 0 errors, ESLint 0 warnings, Vite Build PASS | **AUTHORITATIVE & COMPLETE** |
| **FD-FINAL-VERIFICATION-01** | Frontend Certification Gate | Full end-to-end journey audit (Customer, Grower, Trainee, Seller, Admin) | **CERTIFIED PASS** |
| **SQ-01 → SQ-10** | SonarQube & Quality Gate Remediation | Code smells, Vulnerabilities, Security Hotspots, Coverage | **CERTIFIED PASS** |
| **PAC-01** | Application Readiness & Architecture Acceptance Gate | Baseline certification before end-to-end acceptance | **CURRENT EXECUTION** |

---

## 4. Documentation Conformance & Ambiguity Resolution

1. **No Architecture Inventions:** The system strictly uses the Modular Monolith architecture with Spring Boot REST API backend and React/Vite SPA frontend.
2. **Provider Abstractions Preserved:** Payment, Refund, Shipping, and Notification interfaces remain strictly abstracted without removing or replacing interfaces.
3. **Mock Credentials & Data:** All local/QAT environments strictly enforce mock payment data and mock authentication identities. No production credentials exist.
4. **Admin Route Protection:** All `/admin` frontend routes are strictly guarded by `<ProtectedRoute>` and `<RequireRole roles={['ROLE_ADMIN']}>`. Direct API routes under `/api/v1/admin/**` require authenticated `ROLE_ADMIN` JWT claims.

---

## 5. Decision & Governance Approval

This document serves as the immutable baseline for PAC-01 Application Readiness & Architecture Acceptance.
