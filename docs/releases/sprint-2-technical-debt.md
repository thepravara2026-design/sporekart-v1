# Sporekart v3.0 — Sprint 2 Technical Debt Register

## Overview
This document registers all identified technical debt items for the **Catalog Domain** at the conclusion of Sprint 2.

---

## Technical Debt Items

| Debt ID | Classification | Component | Description | Impact | Current Status | Recommended Future Action |
|---|---|---|---|---|---|---|
| DEBT-01 | Medium (P2) | Backend Database | In-memory pattern matching (`ILIKE` / `LIKE`) for catalog keyword search. | Performance overhead when catalog scale exceeds 500,000 active products. | Deferred | Migrate catalog search to dedicated full-text search engine (Elasticsearch / Meilisearch) in Sprint 7. |
| DEBT-02 | Low (P3) | Frontend Router | React Router v7 future flag deprecation warnings (`v7_startTransition`, `v7_relativeSplatPath`) in test outputs. | Non-blocking console warning in development/test logs. | Accepted | Enable opt-in future flags when upgrading to React Router v7 in a future infrastructure maintenance sprint. |

---

## Technical Debt Summary
- **P0 (Release Blockers)**: 0
- **P1 (High Risk)**: 0
- **P2 (Medium)**: 1 (Deferred to Sprint 7)
- **P3 (Low)**: 1 (Accepted)

---

## Closure Verdict
Zero release-blocking (P0/P1) technical debt items exist. The Catalog vertical is approved for production baseline release.
