# SPOREKART v3.0 — SPRINT 1E REQUIREMENTS TRACEABILITY MATRIX

This document traces all requirements established across Sprint 1 (Catalog Domain Implementation & Integration) to verify completeness and readiness for Sprint 2.

---

## REQUIREMENT TRACEABILITY MATRIX

| Req ID | Requirement Description | Source | Backend Module | Database | API Endpoint | Frontend Component | Test Coverage | Status |
|---|---|---|---|---|---|---|---|---|
| REQ-CAT-001 | Product and Category Domain Models with invariants | Sprint 1B | `modules.catalog.domain` | Flyway V2 Schema | N/A | N/A | `ProductDomainTest`, `CategoryDomainTest` | PASS |
| REQ-CAT-002 | Persistence Repository Adapters & Spring Data JPA | Sprint 1B | `modules.catalog.infrastructure.persistence` | H2 / PostgreSQL Schema | N/A | N/A | `CatalogPersistenceTest` | PASS |
| REQ-CAT-003 | Application Services for Product & Category Querying | Sprint 1C | `modules.catalog.application` | JPA Repository | N/A | N/A | `ProductApplicationServiceTest`, `CategoryApplicationServiceTest` | PASS |
| REQ-CAT-004 | Paginated & Filtered Product REST API | Sprint 1C | `modules.catalog.controller` | SQL Query / Fetch Join | `GET /api/v1/catalog/products` | `ProductListPage`, `catalogApi.getProducts` | `CatalogProductControllerTest`, `CatalogApiIntegrationTest` | PASS |
| REQ-CAT-005 | Product Detail Lookup by UUID or SKU | Sprint 1C | `modules.catalog.controller` | Query by ID / SKU | `GET /api/v1/catalog/products/{productId}` | `ProductDetailPage`, `catalogApi.getProduct` | `CatalogProductControllerTest`, `CatalogApiIntegrationTest` | PASS |
| REQ-CAT-006 | Paginated & Filtered Category REST API | Sprint 1C | `modules.catalog.controller` | SQL Query | `GET /api/v1/catalog/categories` | `CategoryListPage`, `catalogApi.getCategories` | `CatalogCategoryControllerTest`, `CatalogApiIntegrationTest` | PASS |
| REQ-CAT-007 | Category Detail Lookup by UUID or Slug | Sprint 1C | `modules.catalog.controller` | Query by ID / Slug | `GET /api/v1/catalog/categories/{categoryId}` | `CategoryListPage`, `catalogApi.getCategory` | `CatalogCategoryControllerTest`, `CatalogApiIntegrationTest` | PASS |
| REQ-CAT-008 | Sort Field Whitelist Validation | Sprint 1C | `modules.catalog.application` | SQL ORDER BY | `products`, `categories` | `CatalogFilterBar` | Controller & Service tests | PASS |
| REQ-CAT-009 | Page Size Limits (Min 1, Max 100) | Sprint 1C | `modules.catalog.application` | SQL LIMIT | `products`, `categories` | `PaginationControls` | Controller & Service tests | PASS |
| REQ-CAT-010 | Public Security PermitAll for Catalog GET APIs | Sprint 1C | `application.configuration` | N/A | `/api/v1/catalog/**` | `axiosInstance` | `SecurityConfigTest`, `CatalogApiIntegrationTest` | PASS |
| REQ-CAT-011 | Deterministic Catalog Data Seeding | Sprint 1C | `modules.catalog.infrastructure.seed` | H2 Seed Runner | N/A | N/A | `DevProfileTest`, `QatProfileTest` | PASS |
| REQ-CAT-012 | Frontend Catalog API Client & TanStack Query Hooks | Sprint 1D | N/A | N/A | `catalogApi.ts` | `useCatalog.ts` | `catalogApi.test.ts` | PASS |
| REQ-CAT-013 | Responsive Product Cards, Grid, and Detail Views | Sprint 1D | N/A | N/A | N/A | `ProductCard`, `ProductGrid`, `ProductDetailPage` | `ProductCard.test.tsx`, `ProductDetailPage.test.tsx` | PASS |
| REQ-CAT-014 | Server-Side Pagination & URL State Persistence | Sprint 1D | N/A | N/A | `page`, `size`, `sort` | `PaginationControls`, `useSearchParams` | `ProductListPage.test.tsx` | PASS |
| REQ-CAT-015 | UX Loading Skeletons, Empty States, and Error Recovery | Sprint 1D | N/A | N/A | N/A | `ProductListPage`, `ProductDetailPage` | `ProductListPage.test.tsx`, `ProductDetailPage.test.tsx` | PASS |
| REQ-CAT-016 | Catalog Domain Hardening & Full-Stack Release Readiness | Sprint 1E | Full-Stack | H2 DEV / QAT | `/api/v1/catalog/**` | All Frontend Routes | E2E & Full Test Suite | PASS |
