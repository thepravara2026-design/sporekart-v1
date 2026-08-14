# SPOREKART v3.0 — SPRINT 1C FRONTEND HANDOFF SPECIFICATION

This document outlines the authoritative input contract for **Sprint 1D — Frontend Catalog Integration**.

---

## CATALOG API HANDOFF SUMMARY

### Product List:
- **Endpoint**: `/api/v1/catalog/products`
- **Method**: `GET`
- **Parameters**: `page` (default: 0), `size` (default: 20, max: 100), `sort` (default: `createdAt,desc`), `categoryId` (UUID), `status` (string), `search` (string)
- **Allowed Sort Fields**: `name`, `price`, `createdAt`, `updatedAt`, `sku`, `status`
- **Example Request**: `GET /api/v1/catalog/products?page=0&size=20&sort=name,asc`
- **Response**: `ApiResponse<PageResponse<ProductDto>>`

---

### Product Detail:
- **Endpoint**: `/api/v1/catalog/products/{productId}`
- **Method**: `GET`
- **Identifier**: `productId` path parameter
- **Supported Identifiers**: UUID or SKU (e.g., `SKU-LION-001`)
- **Response**: `ApiResponse<ProductDto>` (`200 OK` or `404 Not Found`)

---

### Category List:
- **Endpoint**: `/api/v1/catalog/categories`
- **Method**: `GET`
- **Parameters**: `page` (default: 0), `size` (default: 20, max: 100), `sort` (default: `name,asc`), `status` (string), `search` (string)
- **Allowed Sort Fields**: `name`, `slug`, `createdAt`, `updatedAt`, `status`
- **Response**: `ApiResponse<PageResponse<CategoryDto>>`

---

### Category Detail:
- **Endpoint**: `/api/v1/catalog/categories/{categoryId}`
- **Method**: `GET`
- **Identifier**: `categoryId` path parameter
- **Supported Identifiers**: UUID or Slug (e.g., `medicinal-mushrooms`)
- **Response**: `ApiResponse<CategoryDto>` (`200 OK` or `404 Not Found`)

---

### Pagination:
- `page`: 0-based page index
- `size`: page size (min 1, max 100)
- `totalElements`: total matching records in database
- `totalPages`: total calculated pages
- `first`: boolean indicator for first page
- `last`: boolean indicator for last page

---

### Errors:
- `CATALOG_PRODUCT_NOT_FOUND` (`404 Not Found`)
- `CATALOG_CATEGORY_NOT_FOUND` (`404 Not Found`)
- `CATALOG_INVALID_SORT` (`400 Bad Request`)
- `CATALOG_INVALID_PAGE_SIZE` (`400 Bad Request`)

---

### Environments:
- **DEV**: H2 In-Memory Database + `CatalogDataSeeder`
- **QAT**: H2 In-Memory Database + `CatalogDataSeeder`
- **Production**: PostgreSQL / Supabase

---

### Authentication:
- Catalog `GET` endpoints are publicly accessible for catalog browsing without authentication headers.

---

### External Services:
- None required.
