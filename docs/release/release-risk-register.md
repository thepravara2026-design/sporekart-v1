# SPOREKART v3.0 — Release Risk Register

**Date**: 2026-08-15

---

## 1. Risk Register Matrix

| ID | Risk Description | Severity | Probability | Impact | Mitigation Strategy | Owner Role | Status | Release Blocking? |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **R-01** | Third-party payment gateway (Razorpay) API delay | Medium | Low | Medium | Webhook idempotency + manual admin reconciliation endpoint | Payment Lead | Mitigated | NO |
| **R-02** | Logistics carrier (Shiprocket) API timeout | Medium | Low | Low | Async fulfillment retry + manual dispatch override in admin panel | Shipping Lead | Mitigated | NO |
| **R-03** | Heavy concurrent stock reservation during flash sale | Medium | Low | Low | `@Version` optimistic locking on inventory items returns 409 | Inventory Lead | Mitigated | NO |
| **R-04** | PostgreSQL connection pool exhaustion | High | Low | High | HikariCP pool tuned to max 20 connections with 30s timeout | Database Lead | Mitigated | NO |
| **R-05** | Unauthorized access attempt on Admin endpoints | High | Low | High | Filter chain `hasRole('ADMIN')` + `@PreAuthorize` enforcement | Security Lead | Mitigated | NO |