# SPOREKART v3.0 — CATALOG REST API SPECIFICATION

## 1. OpenAPI 3 Specifications

The backend automatically generates an OpenAPI v3 spec at runtime:
- **OpenAPI JSON**: `GET /v3/api-docs`
- **Swagger UI**: `GET /swagger-ui/index.html`

---

## 2. API Endpoints

### 2.1 Products API
- `GET /api/v1/catalog/products`: Paginated listing with search, category, status, min/max price, and sorting.
- `GET /api/v1/catalog/products/{productId}`: Single product lookup by UUID or SKU.

### 2.2 Categories API
- `GET /api/v1/catalog/categories`: Paginated category listing.
- `GET /api/v1/catalog/categories/{categoryId}`: Single category lookup by UUID or slug.

---

## 3. Query Parameter Contracts

| Parameter | Type | Validation / Constraints | Example |
|---|---|---|---|
| `page` | `Integer` | Min `0`, default `0` | `?page=0` |
| `size` | `Integer` | Min `1`, Max `100`, default `20` | `?size=12` |
| `sort` | `String` | Whitelisted fields (`name`, `price`, `createdAt`, `updatedAt`, `sku`, `status`), direction `asc`/`desc` | `?sort=price,asc` |
| `categoryId` | `UUID` | Valid UUID | `?categoryId=c1f71f65-3850-482a-921c-4e899b80b72a` |
| `status` | `String` | `ACTIVE`, `DRAFT`, `OUT_OF_STOCK`, `DISCONTINUED`, `ARCHIVED` | `?status=ACTIVE` |
| `search` | `String` | Case-insensitive substring match | `?search=oyster` |
| `minPrice` | `Decimal` | Min `0.00` | `?minPrice=10.00` |
| `maxPrice` | `Decimal` | Min `0.00`, `minPrice <= maxPrice` | `?maxPrice=50.00` |

---

## 4. Response Contracts

### 4.1 Success Response (`ApiResponse<T>`)
```json
{
  "success": true,
  "data": { ... }
}
```

### 4.2 Error Response (`ApiErrorResponse`)
```json
{
  "success": false,
  "error": {
    "code": "CATALOG_INVALID_PRICE_RANGE",
    "message": "Minimum price cannot be greater than maximum price",
    "timestamp": "2026-08-15T00:30:00Z",
    "path": "/api/v1/catalog/products",
    "requestId": "req-998822"
  }
}
```
