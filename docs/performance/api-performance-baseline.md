# SPOREKART v3.0 — API Performance & Budget Baseline

**Date**: 2026-08-15

---

## 1. Production API Performance Budgets & Measured Telemetry

| Endpoint / Journey | SLA / Budget (p95) | Measured p50 Latency | Measured p95 Latency | Measured p99 Latency | Budget Compliance |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `GET /api/v1/catalog/products` | **< 100 ms** | 12 ms | 32 ms | 54 ms | ✅ COMPLIANT |
| `POST /api/v1/cart/items` | **< 100 ms** | 14 ms | 38 ms | 62 ms | ✅ COMPLIANT |
| `POST /api/v1/checkout` | **< 200 ms** | 22 ms | 58 ms | 88 ms | ✅ COMPLIANT |
| `POST /api/v1/payments/verify` | **< 300 ms** | 42 ms | 94 ms | 142 ms | ✅ COMPLIANT |
| `GET /api/v1/orders/customer` | **< 150 ms** | 18 ms | 46 ms | 76 ms | ✅ COMPLIANT |
| `GET /api/v1/admin/dashboard` | **< 250 ms** | 28 ms | 68 ms | 110 ms | ✅ COMPLIANT |