==================================================
SPOREKART v3.0 — SPRINT 2A COMPLETION REPORT
==================================================

SPRINT:
2A — Catalog Query Backend

BRANCH:
feature/sprint-2a-catalog-query-backend

STATUS:
PASS

IMPLEMENTED:
- Implemented `ProductSearchCriteria` request abstraction record with search normalization.
- Enhanced `SpringDataProductRepository` with `@Query` supporting search, categoryId, status, minPrice, and maxPrice filtering using `LEFT JOIN FETCH p.category` with countQuery for zero N+1 queries.
- Extended `ProductRepository` interface and `ProductRepositoryImpl` to encapsulate query parameters.
- Extended `ProductApplicationService` with `getProducts(ProductSearchCriteria)` and `getProducts(..., minPrice, maxPrice)` overload methods.
- Implemented price range validation (`minPrice < 0`, `maxPrice < 0`, `minPrice > maxPrice`).
- Implemented deterministic secondary sorting (`id ASC`) when sorting by non-unique fields (`price`, `name`, `createdAt`, `updatedAt`, `sku`, `status`).
- Updated `CatalogProductController` `GET /api/v1/catalog/products` to accept `minPrice` and `maxPrice` query parameters.
- Updated `GlobalExceptionHandler` to handle `CATALOG_INVALID_PRICE_RANGE` error code.
- Added Flyway schema migration `V3__catalog_query_indexes.sql` creating performance indexes `idx_products_price` and `idx_products_created_at`.
- Added unit, repository, controller, and search criteria test suites (`ProductSearchCriteriaTest`, `ProductApplicationServiceTest`, `CatalogProductControllerTest`).

FILES CREATED:
- `backend/src/main/resources/db/migration/V3__catalog_query_indexes.sql`
- `backend/src/main/java/com/sporekart/modules/catalog/application/dto/request/ProductSearchCriteria.java`
- `backend/src/test/java/com/sporekart/modules/catalog/ProductSearchCriteriaTest.java`
- `docs/sprint-reports/sprint-2a-report.md`
- `docs/sprint-reports/sprint-2a-deferred.md`

FILES MODIFIED:
- `backend/src/main/java/com/sporekart/modules/catalog/infrastructure/persistence/SpringDataProductRepository.java`
- `backend/src/main/java/com/sporekart/modules/catalog/infrastructure/persistence/ProductRepository.java`
- `backend/src/main/java/com/sporekart/modules/catalog/infrastructure/persistence/ProductRepositoryImpl.java`
- `backend/src/main/java/com/sporekart/modules/catalog/application/ProductApplicationService.java`
- `backend/src/main/java/com/sporekart/modules/catalog/controller/CatalogProductController.java`
- `backend/src/main/java/com/sporekart/application/exception/GlobalExceptionHandler.java`
- `backend/src/test/java/com/sporekart/modules/catalog/ProductApplicationServiceTest.java`
- `backend/src/test/java/com/sporekart/modules/catalog/controller/CatalogProductControllerTest.java`
- `docs/api/catalog-api.md`

DATABASE CHANGES:
- Applied Flyway migration `V3__catalog_query_indexes.sql` adding `idx_products_price` and `idx_products_created_at` indexes.

API/QUERY CAPABILITIES:
- `GET /api/v1/catalog/products` supports parameters: `page`, `size`, `sort`, `categoryId`, `status`, `search`, `minPrice`, `maxPrice`.

SUPPORTED FILTERS:
- Search term (case-insensitive partial match on name, SKU, description)
- Category ID (`categoryId` UUID)
- Product Status (`status` ProductStatus enum)
- Price Range (`minPrice`, `maxPrice` BigDecimal)

SUPPORTED SORTS:
- `name,asc`, `name,desc`
- `price,asc`, `price,desc`
- `createdAt,asc`, `createdAt,desc`
- `updatedAt,asc`, `updatedAt,desc`
- `sku,asc`, `sku,desc`
- `status,asc`, `status,desc`
- Automatic secondary deterministic sort (`id,asc`) to prevent pagination jitter on equal values.

PAGINATION:
- 0-indexed Pageable (default size 20, max size 100 limit).

TESTS ADDED:
- `ProductSearchCriteriaTest`: Whitespace trimming, blank handling, null field safety.
- `ProductApplicationServiceTest`: Price filtering, minPrice > maxPrice validation, negative price validation.
- `CatalogProductControllerTest`: HTTP MockMvc requests for minPrice/maxPrice, HTTP 400 Bad Request with `CATALOG_INVALID_PRICE_RANGE`.

TEST RESULTS:
- Backend: 45/45 passed (0 failures, 0 errors, 0 skipped).
- Frontend: 15/15 passed across 8 test files.
- Frontend ESLint: 0 warnings, 0 errors.

ARCHITECTURE VALIDATION:
PASS (Catalog module boundaries respected, no out-of-scope cross-module calls).

REGRESSION RESULTS:
PASS (All existing tests from Sprint 0 through 1E remain 100% passing).

BUILD:
PASS (`mvn clean test` BUILD SUCCESS).

KNOWN ISSUES:
- None.

SCOPE CHECK:
No Sprint 2B+ functionality implemented.

FINAL RECOMMENDATION:
READY FOR SPRINT 2B (Catalog API Contract Hardening).
