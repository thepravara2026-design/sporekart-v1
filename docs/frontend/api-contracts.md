# FD-02 — Authoritative API Contracts Specification

**Sprint:** FD-02  
**Repository:** `f:/sporekart-v3.0`  
**Date:** 2026-08-18  
**Status:** Complete  

---

## 1. Global Standard Response Envelopes

### 1.1 Success Response Wrapper
All Spring Boot REST controllers returning wrapped resources conform to the standard `ApiResponse<T>` envelope:

```json
{
  "success": true,
  "data": { ... }
}
```

### 1.2 Error Response Wrapper
All backend exception handlers (`GlobalExceptionHandler.java`) return standard error envelopes:

```json
{
  "success": false,
  "error": {
    "code": "BAD_REQUEST",
    "message": "Validation failed for field 'email'",
    "timestamp": "2026-08-18T11:15:00.000Z",
    "path": "/api/v1/auth/login",
    "requestId": "req-8f4b-1029"
  }
}
```

### 1.3 Standard Page Response Specification
All paginated endpoints return `PageResponse<T>` conforming to Spring Data `Page` serialization:

```json
{
  "content": [ ... ],
  "page": 0,
  "size": 20,
  "totalElements": 142,
  "totalPages": 8,
  "first": true,
  "last": false
}
```

---

## 2. Authentication & Authorization Contract

- **Token Type:** OAuth 2.0 Bearer Token (JWT).
- **Request Authorization Header:** `Authorization: Bearer <accessToken>`
- **Token Interceptor Policy:** Configured in [`apiClient.ts`](file:///f:/sporekart-v3.0/frontend/src/services/apiClient.ts) to automatically extract token from `localStorage` (`accessToken` or `token`) and attach to outgoing HTTP requests.
- **Header Fallback:** Legacy / mock headers (`X-Customer-Id`, `X-Admin-Id`) supported during integration transitions where JWT is omitted.

---

## 3. Data Representation Contracts

### 3.1 Date & Time Formatting
- **Format:** ISO-8601 UTC Strings (e.g. `2026-08-18T11:15:00Z`).
- **Parsing:** Handled on frontend as native JavaScript `Date` or formatted strings via standard locale formatters.

### 3.2 Monetary Representations
- **Format:** Numeric decimals (e.g., `24.99`, `150.00`).
- **Currency Code:** ISO 4217 Currency String (`INR` by default).
- **Rule:** No client-side floating point rounding for transaction math; server-authoritative checkout calculation (`/api/v1/checkout/preview`) determines final totals.

### 3.3 Identifier Types
- **Product / Category / Order / Return / User IDs:** UUID strings (e.g., `c1f71f65-3850-482a-921c-4e899b80b72a`) or Human-readable reference codes (e.g., `SKU-LION-001`, `RET-2026-0012`).

---

## 4. Domain Service Contract Definitions

### 4.1 Auth Service (`authApi.ts`)
- `POST /api/v1/auth/register` → `RegisterRequestDto` → `UserProfileDto` (Status 201)
- `POST /api/v1/auth/login` → `LoginRequestDto` → `AuthTokenResponseDto` (Status 200)
- `POST /api/v1/auth/refresh` → `RefreshTokenRequestDto` → `AuthTokenResponseDto` (Status 200)
- `POST /api/v1/auth/logout` → `204 No Content`
- `GET /api/v1/auth/me` → `UserProfileDto` (Status 200)

### 4.2 Cart & Checkout Service (`cartApi.ts`)
- `GET /api/v1/cart` → `ApiResponse<CartDto>`
- `POST /api/v1/cart/items` → `AddCartItemCommand` → `ApiResponse<CartDto>` (Status 201)
- `PATCH /api/v1/cart/items/{itemId}` → `UpdateCartItemCommand` → `ApiResponse<CartDto>`
- `DELETE /api/v1/cart/items/{itemId}` → `ApiResponse<CartDto>`
- `POST /api/v1/checkout/preview` → `CheckoutPreviewRequest` → `ApiResponse<CheckoutPreviewResponse>`

### 4.3 Catalog Service (`catalogApi.ts`)
- `GET /api/v1/catalog/products` → `ProductQueryParams` → `ApiResponse<PageResponse<ProductDto>>`
- `GET /api/v1/catalog/products/{productId}` → `ApiResponse<ProductDto>`
- `GET /api/v1/catalog/categories` → `CategoryQueryParams` → `ApiResponse<PageResponse<CategoryDto>>`
