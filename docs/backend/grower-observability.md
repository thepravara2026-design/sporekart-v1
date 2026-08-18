# SPOREKART v3.0 — GROWER OBSERVABILITY & LOGGING SPECIFICATION

**Sprint ID:** GB-03  
**Sprint Name:** Grower Production Readiness, Observability & Operational Hardening  
**Date:** August 19, 2026  
**Status:** COMPLETE / VERIFIED  

---

## 1. Observability Architecture

Every Grower REST API call traversing `/api/v1/grower/*` is integrated with SPOREKART's core observability layer:
1. `RequestIdFilter` & `CorrelationAndTracingFilter`: Injects `requestId` and MDC correlation headers.
2. Structured SLF4J Logging: Service operations emit structured log messages answering: Who? What? Which grower? Which resource? When? Outcome.

---

## 2. Audit & Event Types

The following domain audit event types are registered in `AuditEventType` and emitted via `SecurityAuditService`:

| Audit Event Type | Trigger Method | Audit Details |
| :--- | :--- | :--- |
| `GROWER_PROFILE_UPDATED` | `updateProfile` | Actor ID, Grower Profile ID, Business Name |
| `GROWER_SETTINGS_UPDATED` | `updateSettings` | Actor ID, Grower Profile ID, Threshold Value |
| `GROWER_PRODUCT_CREATED` | `createProduct` | Actor ID, Product UUID, SKU |
| `GROWER_PRODUCT_UPDATED` | `updateProduct` | Actor ID, Product UUID |
| `GROWER_STOCK_ADJUSTED` | `adjustStock` | Actor ID, Inventory Item UUID, Old Qty -> New Qty |
| `GROWER_ORDER_TRANSITIONED` | `transitionOrder` | Actor ID, Order UUID, Old Status -> New Status |

---

## 3. Log Safety & Compliance

- Credentials, JWT tokens, and customer PII are strictly excluded from log statements.
- Structured logger prefix `GROWER_AUDIT` allows centralized log indexers (e.g. ELK, CloudWatch, Datadog) to parse Grower audit events cleanly.
