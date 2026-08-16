# SPOREKART v3.0 — SPRINT 6E PERFORMANCE BASELINE

## 1. Environment Baseline

- **Date**: 2026-08-15
- **Branch**: `sprint-6e-performance-hardening`
- **Base Commit**: `46419c4` (Sprint 6D API & Contract Hardening)
- **Environment**: Windows 11 / Java 21.0.11 / Node.js v20+ / Vite 5.4.21 / Spring Boot 3.4.2 / H2 (Test/Dev)
- **Backend Tests Baseline**: 329/329 passed (319 domain/integration + 10 API contract tests)
- **Frontend Tests Baseline**: 20/20 passed (10 test files)

---

## 2. Frontend Build Baseline

- **Build Time**: 2.42s
- **Transformed Modules**: 151 modules
- **Bundle Breakdown**:
  - `dist/index.html`: 0.50 kB (gzip: 0.34 kB)
  - `dist/assets/index-B0aH6HZb.css`: 8.17 kB (gzip: 2.12 kB)
  - `dist/assets/index-CRhHIgKv.js`: 278.45 kB (gzip: 90.63 kB)
- **Total Frontend Asset Size**: ~287.12 kB uncompressed (93.09 kB gzipped)

---

## 3. Backend & API Latency Baseline (Pre-Optimization Targets)

| Endpoint | Category | Target Baseline Latency (p50 / p95 / p99) | DB Round-trips / Notes |
| :--- | :--- | :--- | :--- |
| `GET /actuator/health` | Health Check | < 50ms | 0 DB queries (lightweight probe) |
| `GET /api/v1/catalog/products` | Catalog List | < 300ms | Paginated (default page size: 20) |
| `GET /api/v1/catalog/products/{id}` | Product Detail | < 250ms | Single product lookup + category details |
| `GET /api/v1/cart` | Cart Operations | < 350ms | Customer cart & line items retrieval |
| `POST /api/v1/checkout/preview` | Checkout Preview | < 500ms | Cart line pricing & server totals |
| `GET /api/v1/orders` | Order History | < 400ms | Customer order summary list |
| `GET /api/v1/orders/{ref}` | Order Detail | < 450ms | Order items, timeline, payment status |

---

## 4. Key Performance Focus Areas for Sprint 6E

1. **HTTP Compression**: Enable GZIP response compression in Spring Boot for JSON/HTML/CSS payload size reduction.
2. **N+1 Query Elimination & DB Batching**: Inspect entity graphs and repository queries across catalog, cart, order, and notification modules.
3. **Pagination Guardrails**: Enforce strict caps on page size (`max size: 50` or `100`) across all list controllers to prevent unbounded memory/DB loading.
4. **Caching Layer**: Enable Spring Caching (`@EnableCaching`) for read-heavy, low-volatility data (e.g. Catalog Categories & Product details).
5. **Frontend Code-Splitting**: Optimize Vite chunk splitting for non-essential routes (e.g., Health, NotFound, Admin pages).
6. **Concurrency & Performance Verification**: Implement `PerformanceHardeningTestSuite.java` to test latency, throughput, concurrency stability, and payload optimization.
