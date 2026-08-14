# SPOREKART v3.0 — CATALOG TECHNICAL DEBT REGISTER

## Debt Classifications
- **Critical**: 0 items
- **High**: 0 items
- **Medium**: 1 item
- **Low**: 1 item

---

## Registered Items

### 1. [Medium] External Search Engine Integration
- **Description**: Current text search relies on database `ILIKE` pattern queries across product name, SKU, and description.
- **Reason**: Adequate for initial catalog volumes (< 100,000 items) and avoids external infrastructure overhead in early sprints.
- **Impact**: Sub-second search for current scale, but full text indexing (e.g., Elasticsearch / Meilisearch) will be required when catalog grows past 500k active items.
- **Suggested Future Action**: Evaluate Elasticsearch / OpenSearch integration in Sprint 7.

### 2. [Low] React Router v7 Opt-In Warnings
- **Description**: Vitest console logs display React Router v7 future flag deprecation warnings (`v7_startTransition`, `v7_relativeSplatPath`).
- **Reason**: Current frontend stack uses React Router 6.x.
- **Impact**: Zero runtime impact or functional failure.
- **Suggested Future Action**: Enable v7 future flags in `BrowserRouter` config during frontend maintenance release.
