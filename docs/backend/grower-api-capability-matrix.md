# SPOREKART v3.0 — GROWER API CAPABILITY MATRIX

**Sprint ID:** GB-03  
**Sprint Name:** Grower Production Readiness, Observability & Operational Hardening  
**Date:** August 19, 2026  
**Status:** COMPLETE / VERIFIED  

---

## 1. Grower Endpoint Capability Matrix

All endpoints require `ROLE_GROWER` authority and resolve identity via authenticated security principal.

| Method | Path | Description | Identity Resolution | Data Scoping | Audit Logged |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/grower/profile` | Get caller grower profile | `@AuthenticationPrincipal` | `findByUserId` | No (Read) |
| `PUT` | `/api/v1/grower/profile` | Update caller grower profile | `@AuthenticationPrincipal` | `findByUserId` | **Yes (`GROWER_PROFILE_UPDATED`)** |
| `GET` | `/api/v1/grower/settings` | Get caller grower settings | `@AuthenticationPrincipal` | `findByUserId` | No (Read) |
| `PUT` | `/api/v1/grower/settings` | Update caller grower settings | `@AuthenticationPrincipal` | `findByUserId` | **Yes (`GROWER_SETTINGS_UPDATED`)** |
| `GET` | `/api/v1/grower/dashboard` | Get operational metrics | `@AuthenticationPrincipal` | `findAllByGrowerId` | No (Read) |
| `GET` | `/api/v1/grower/products` | List grower products | `@AuthenticationPrincipal` | `findAllByGrowerId` | No (Read) |
| `POST` | `/api/v1/grower/products` | Create grower product | `@AuthenticationPrincipal` | `setGrowerId(userId)` | **Yes (`GROWER_PRODUCT_CREATED`)** |
| `GET` | `/api/v1/grower/products/{id}` | Get product details | `@AuthenticationPrincipal` | Ownership Check | No (Read) |
| `PUT` | `/api/v1/grower/products/{id}` | Update product details | `@AuthenticationPrincipal` | Ownership Check | **Yes (`GROWER_PRODUCT_UPDATED`)** |
| `GET` | `/api/v1/grower/inventory` | List grower inventory | `@AuthenticationPrincipal` | `findAllByGrowerId` | No (Read) |
| `POST` | `/api/v1/grower/inventory/{sku}/adjust` | Adjust stock quantity | `@AuthenticationPrincipal` | Ownership Check | **Yes (`GROWER_STOCK_ADJUSTED`)** |
| `GET` | `/api/v1/grower/orders` | List grower orders | `@AuthenticationPrincipal` | `findAllByGrowerId` | No (Read) |
| `GET` | `/api/v1/grower/orders/{id}` | Get order details | `@AuthenticationPrincipal` | Ownership Check | No (Read) |
| `POST` | `/api/v1/grower/orders/{id}/status` | Transition order status | `@AuthenticationPrincipal` | Ownership Check | **Yes (`GROWER_ORDER_TRANSITIONED`)** |
| `GET` | `/api/v1/grower/shipments` | List grower shipments | `@AuthenticationPrincipal` | `findAllByGrowerId` | No (Read) |
| `GET` | `/api/v1/grower/reports/summary` | Get report summary | `@AuthenticationPrincipal` | Aggregate Scope | No (Read) |
