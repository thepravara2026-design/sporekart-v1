# SPOREKART v3.0 — CATALOG REST API CONTRACT SPECIFICATION (SPRINT 2A)

This document establishes the official REST API contract between the Spring Boot backend catalog module and the Vite/React frontend application for **Sprint 2A — Catalog Query Backend**.

---

## 1. BASE CONFIGURATION

- **Base URL**: `/api/v1/catalog`
- **Protocol**: HTTP/1.1 REST
- **Content-Type**: `application/json`
- **Authentication**: Public `GET` access enabled for catalog browsing. No authentication headers required for catalog queries.

---

## 2. COMMON RESPONSE CONTRACTS

### 2.1 Success Response Wrapper (`ApiResponse<T>`)
```json
{
  "success": true,
  "data": { ... }
}
```

### 2.2 Standard Pagination Wrapper (`PageResponse<T>`)
```json
{
  "success": true,
  "data": {
    "content": [ ... ],
    "page": 0,
    "size": 20,
    "totalElements": 100,
    "totalPages": 5,
    "first": true,
    "last": false
  }
}
```

### 2.3 Error Response Wrapper (`ApiErrorResponse`)
```json
{
  "success": false,
  "error": {
    "code": "CATALOG_PRODUCT_NOT_FOUND",
    "message": "Product not found with ID: f9b70f55-6674-4bd0-a237-f84689778745",
    "timestamp": "2026-08-14T23:15:00Z",
    "path": "/api/v1/catalog/products/f9b70f55-6674-4bd0-a237-f84689778745",
    "requestId": "50df2c53-b9b5-4b13-a442-998ca99eb572"
  }
}
```

---

## 3. PRODUCTS API

### 3.1 Get Paginated Products
- **Method**: `GET`
- **Endpoint**: `/api/v1/catalog/products`
- **Query Parameters**:
  - `page` (int, default: `0`, min: `0`)
  - `size` (int, default: `20`, min: `1`, max: `100`)
  - `sort` (string, e.g., `name,asc`, `price,desc`, `createdAt,desc`)
    - *Allowed sort fields*: `name`, `price`, `createdAt`, `updatedAt`, `sku`, `status`
  - `categoryId` (UUID, optional)
  - `status` (string, optional: `DRAFT`, `ACTIVE`, `OUT_OF_STOCK`, `DISCONTINUED`, `ARCHIVED`)
  - `search` (string, optional substring search across name, SKU, and description)
  - `minPrice` (decimal, optional minimum price filter, min: `0.00`)
  - `maxPrice` (decimal, optional maximum price filter, min: `0.00`)

- **Example Response**: `200 OK`
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": "e4a282f1-2856-4b2a-8991-965a782b79a1",
        "sku": "SKU-LION-001",
        "name": "Lion's Mane Extract",
        "description": "Organic Lion's Mane mushroom extract for cognitive focus.",
        "price": 29.99,
        "currency": "USD",
        "status": "ACTIVE",
        "category": {
          "id": "c1f71f65-3850-482a-921c-4e899b80b72a",
          "name": "Medicinal Mushrooms",
          "slug": "medicinal-mushrooms",
          "description": "Health and wellness mushroom extracts and powders",
          "status": "ACTIVE",
          "createdAt": "2026-08-14T23:10:00Z",
          "updatedAt": "2026-08-14T23:10:00Z"
        },
        "createdAt": "2026-08-14T23:10:00Z",
        "updatedAt": "2026-08-14T23:10:00Z"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 1,
    "totalPages": 1,
    "first": true,
    "last": true
  }
}
```

### 3.2 Get Product Detail
- **Method**: `GET`
- **Endpoint**: `/api/v1/catalog/products/{productId}`
- **Path Parameter**: `productId` (UUID string or SKU string)
- **Response**: `200 OK` on success, `404 Not Found` if missing.

---

## 4. CATEGORIES API

### 4.1 Get Paginated Categories
- **Method**: `GET`
- **Endpoint**: `/api/v1/catalog/categories`
- **Query Parameters**:
  - `page` (int, default: `0`, min: `0`)
  - `size` (int, default: `20`, min: `1`, max: `100`)
  - `sort` (string, e.g., `name,asc`, `slug,asc`, `createdAt,desc`)
    - *Allowed sort fields*: `name`, `slug`, `createdAt`, `updatedAt`, `status`
  - `status` (string, optional: `ACTIVE`, `INACTIVE`)
  - `search` (string, optional substring search across name, slug, description)

- **Example Response**: `200 OK`
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": "c1f71f65-3850-482a-921c-4e899b80b72a",
        "name": "Medicinal Mushrooms",
        "slug": "medicinal-mushrooms",
        "description": "Health and wellness mushroom extracts and powders",
        "status": "ACTIVE",
        "createdAt": "2026-08-14T23:10:00Z",
        "updatedAt": "2026-08-14T23:10:00Z"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 1,
    "totalPages": 1,
    "first": true,
    "last": true
  }
}
```

### 4.2 Get Category Detail
- **Method**: `GET`
- **Endpoint**: `/api/v1/catalog/categories/{categoryId}`
- **Path Parameter**: `categoryId` (UUID string or slug string)
- **Response**: `200 OK` on success, `404 Not Found` if missing.

---

## 5. HTTP STATUS CODES & ERROR CODES

| HTTP Status | Error Code | Description |
|---|---|---|
| `200 OK` | N/A | Successful operation |
| `400 Bad Request` | `CATALOG_INVALID_PAGE_SIZE` | Page size < 1 or exceeds max limit of 100 |
| `400 Bad Request` | `CATALOG_INVALID_SORT` | Sort field not in whitelist or invalid direction |
| `400 Bad Request` | `CATALOG_INVALID_PRICE_RANGE` | Negative min/max price or minPrice > maxPrice |
| `404 Not Found` | `CATALOG_PRODUCT_NOT_FOUND` | Product with given ID/SKU does not exist |
| `404 Not Found` | `CATALOG_CATEGORY_NOT_FOUND` | Category with given ID/slug does not exist |
| `500 Internal Server Error` | `INTERNAL_SERVER_ERROR` | Unhandled server error |
