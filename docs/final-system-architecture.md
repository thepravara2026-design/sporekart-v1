# SPOREKART v3.0 — Final System Architecture

**Date**: 2026-08-15
**Version**: v3.0.0
**Target Environment**: Kubernetes / Docker / AWS ECS / Cloud Run

---

## 1. System Topology

```
+-----------------------------------------------------------------------+
|                            Client Tier                                |
|  React 18 + TypeScript SPA (Vite Production Build)                     |
+-----------------------------------------------------------------------+
                                   | HTTPS / REST (JSON)
                                   v
+-----------------------------------------------------------------------+
|                            Gateway / Security                         |
|  Tomcat Embedded (Spring Security + RateLimitingFilter + RequestId)   |
+-----------------------------------------------------------------------+
                                   | In-Memory Method Invocation
                                   v
+-----------------------------------------------------------------------+
|                    Modular Monolith Application Core                  |
|                                                                       |
|  [Catalog]  [Cart]  [Checkout]  [Order]  [Payment]  [Inventory]       |
|  [Shipment] [Return] [Refund]   [Support] [Review]  [Notification]     |
|                                                                       |
|  Spring Application Context / Spring Data JPA                         |
+-----------------------------------------------------------------------+
         |                                 |                   |
         v                                 v                   v
+------------------+             +--------------------+ +-------------------+
|  Primary Store   |             | External Providers | | Event / Async     |
|  PostgreSQL 16   |             | - Razorpay         | | Spring Event      |
|  HikariCP Pool   |             | - Shiprocket       | | Listener Bus     |
|  Flyway V1-V16   |             | - SMTP/SMS Gateway | | (Outbox Ready)  |
+------------------+             +--------------------+ +-------------------+
```

---

## 2. Core Operational Flow

1. **Request Ingestion**: Incoming HTTP requests pass through `RequestIdFilter` (MDC correlation assignment) and `RateLimitingFilter` (per-IP sliding window check).
2. **Security & Authentication**: `SecurityConfig` validates authentication tokens / sessions and enforces `@PreAuthorize("hasRole('ADMIN')")` on management routes.
3. **Application Services**: Requests execute within `@Transactional` boundaries defined by domain `ApplicationService` classes.
4. **Data Access**: Spring Data JPA repositories execute parameterized queries against PostgreSQL via HikariCP managed connections.
5. **Domain Events**: State changes emit Spring `ApplicationEvent` notifications. `TransactionalEventListener` guarantees downstream triggers (e.g. shipment creation, stock reservation, email dispatch) execute upon transaction commit.

---

## 3. External Systems Integration Architecture

- **Payment Provider (Razorpay)**: HTTPS API calls for payment order initialization & signature verification. Inbound webhooks validated via HMAC-SHA256.
- **Shipping Provider (Shiprocket)**: REST API integration for shipment creation, AWB generation, and tracking webhook ingestion.
- **Notification Provider**: SMTP / SMS Gateway integration triggered asynchronously via `NotificationEventListener`.