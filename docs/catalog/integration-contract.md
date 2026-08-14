# Sporekart v3.0 — Catalog Integration Contract

## Overview
This document defines the strict integration contract for consuming the **Catalog Domain** from future Sporekart modules (e.g., Cart, Checkout, Orders).

---

## 1. Allowed Integration Patterns (What Future Modules MAY Consume)

### A. HTTP REST APIs
Future modules and external clients MUST communicate with the Catalog domain via its published HTTP APIs:
- `GET /api/v1/catalog/products` (Product listing, filtering, search, pagination)
- `GET /api/v1/catalog/products/{productId}` (Product lookup by ID)
- `GET /api/v1/catalog/categories` (Category listing)
- `GET /api/v1/catalog/categories/{categoryId}` (Category lookup by ID)

### B. Public DTO Contracts
Backend modules within the monolith MUST consume Catalog data through public DTO classes:
- `com.sporekart.modules.catalog.application.dto.response.ProductDto`
- `com.sporekart.modules.catalog.application.dto.response.CategoryDto`
- `com.sporekart.modules.catalog.application.dto.request.ProductSearchCriteria`

### C. Stable Identifiers
- `productId` (`java.util.UUID`): Primary key identifying a product.
- `sku` (`java.lang.String`): Business stock keeping unit.
- `categoryId` (`java.util.UUID`): Primary key identifying a category.

---

## 2. Forbidden Integration Patterns (What Future Modules MUST NOT Consume)

> [!CAUTION]
> Direct internal dependencies create tight architectural coupling and destroy modularity.

1. **NO Direct JPA Entity Imports**:
   Future modules (e.g., Cart) MUST NOT import `com.sporekart.modules.catalog.infrastructure.persistence.ProductEntity`.
2. **NO Repository Sharing**:
   Future modules MUST NOT inject or call `ProductRepository` or `CategoryRepository`.
3. **NO Direct Database Joins**:
   Future module database schemas MUST NOT introduce direct SQL `FOREIGN KEY` constraints or table joins to `products` or `categories` tables without explicit service boundary abstractions.
4. **NO Direct Internal State Mutation**:
   Catalog domain state (e.g., product prices, status) MUST NOT be mutated by external services except through explicit, authorized Catalog domain APIs.

---

## 3. Error Handling Contract
Catalog APIs return standard error payloads conforming to `ApiErrorResponse`:
- HTTP 400 Bad Request: Invalid input parameters or unsupported sort field.
- HTTP 404 Not Found: Product or Category ID does not exist.
- HTTP 500 Internal Server Error: Unexpected server error (sanitized public message).
