# SPOREKART v3.0 — Sprint 3J Authorization Matrix

The following matrix documents the server-side security authorization decisions for all public, customer, and administrative commerce API endpoints in SPOREKART v3.0.

---

### Endpoint Authorization Decisions

| Endpoint | HTTP Method | Anonymous | Customer | Admin | Owner Enforcement | Authorization Decision / Mechanism |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| `/api/v1/catalog/products` | GET | Allowed | Allowed | Allowed | N/A | Public catalog browsing allowed |
| `/api/v1/catalog/products/{id}` | GET | Allowed | Allowed | Allowed | N/A | Public product detail access allowed |
| `/api/v1/catalog/categories` | GET | Allowed | Allowed | Allowed | N/A | Public category navigation allowed |
| `/api/v1/cart/items` | POST | Denied | Allowed | Allowed | `customerId` | Authenticated customer session / IDOR check |
| `/api/v1/cart/items/{id}` | PUT/DELETE | Denied | Allowed | Allowed | `customerId` | Authenticated customer session / IDOR check |
| `/api/v1/cart` | GET/DELETE | Denied | Allowed | Allowed | `customerId` | Cart ownership matched to active customer |
| `/api/v1/checkout/preview` | POST | Denied | Allowed | Allowed | `customerId` | Authoritative server price computation |
| `/api/v1/checkout/orders` | POST | Denied | Allowed | Allowed | `customerId` | Atomic order creation + inventory lock |
| `/api/v1/orders` | GET | Denied | Allowed | Allowed | `customerId` | Filtered strictly to authenticated customer |
| `/api/v1/orders/{id}` | GET | Denied | Allowed | Allowed | `customerId` | IDOR check: matching customer or `ROLE_ADMIN` |
| `/api/v1/orders/{id}/cancel` | POST | Denied | Allowed | Allowed | `customerId` | IDOR check + Order State Machine validation |
| `/api/v1/payments/verify` | POST | Denied | Allowed | Allowed | `customerId` | Idempotent callback validation |
| `/api/v1/payments/webhook` | POST | Allowed (HMAC) | N/A | N/A | Webhook Signature | Cryptographic HMAC signature verification |
| `/api/v1/shipments/{id}` | GET | Denied | Allowed | Allowed | `customerId` | Customer order ownership check |
| `/api/v1/admin/shipments/**` | POST/PUT | Denied | Denied | Allowed (`ROLE_ADMIN`) | N/A | Strict administrative role requirement |
| `/api/v1/returns` | POST/GET | Denied | Allowed | Allowed | `customerId` | Customer return ownership check |
| `/api/v1/returns/{id}/inspect` | POST | Denied | Denied | Allowed (`ROLE_ADMIN`) | N/A | Administrative return inspection permission |
