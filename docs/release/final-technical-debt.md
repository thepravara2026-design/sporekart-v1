# SPOREKART v3.0 — Final Technical Debt Register

**Date**: 2026-08-15
**Status**: All items classified as non-blocking technical debt.

---

## 1. Technical Debt Register

| Debt ID | Summary | Impact | Severity | Release Blocking? | Target Sprint |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **DEBT-001** | Mock Payment Gateway in DEV/QAT profile | Development testing convenience | LOW | NO | Post Go-Live |
| **DEBT-002** | In-memory Rate Limiting Filter | Single-instance bound rate limiter | MEDIUM | NO | Post Go-Live (when >2 nodes) |
| **DEBT-003** | In-process Outbox Event Bus | Events execute within application process | LOW | NO | Post Go-Live (high throughput) |