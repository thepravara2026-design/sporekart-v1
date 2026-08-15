# SPOREKART v3.0 — Final Access Control Matrix

**Date**: 2026-08-15
**Security Model**: Role-Based Access Control (RBAC) + Resource Ownership Validation (IDOR Protection)

---

## 1. Access Control Matrix Across Roles

| API Endpoint Pattern | Anonymous | Customer (Authenticated) | Admin (`ROLE_ADMIN`) | Access Control Mechanism |
| :--- | :--- | :--- | :--- | :--- |
| `GET /api/v1/health` | ALLOW | ALLOW | ALLOW | Public Endpoint |
| `GET /api/v1/catalog/**` | ALLOW | ALLOW | ALLOW | Public Catalog Reads |
| `GET /api/v1/products/*/reviews` | ALLOW | ALLOW | ALLOW | Public Product Reviews |
| `GET /api/v1/cart` | DENY (401) | ALLOW (Own Cart) | ALLOW | Session / Customer ID Binding |
| `POST /api/v1/orders` | DENY (401) | ALLOW (Authenticated) | ALLOW | Spring Security Auth Principal |
| `GET /api/v1/orders/{ref}` | DENY (401) | ALLOW (Own Order Only) | ALLOW | Ownership Check (`resolveCustomerId`) |
| `POST /api/v1/orders/{ref}/returns` | DENY (401) | ALLOW (Own Order Only) | ALLOW | Ownership Check (`resolveCustomerId`) |
| `POST /api/v1/customer/support/tickets` | DENY (401) | ALLOW (Own Ticket) | ALLOW | Ownership Check (`resolveCustomerId`) |
| `POST /api/v1/reviews` | DENY (401) | ALLOW (Authenticated) | ALLOW | Ownership Check (`resolveCustomerId`) |
| `GET /api/v1/admin/**` | DENY (401) | DENY (403) | ALLOW | `@PreAuthorize("hasRole('ADMIN')")` |

---

## 2. Server-Side IDOR Enforcement Principle

No customer parameter passed in path or query strings (e.g. `customerId=cust-999`) is trusted. All user identities are resolved exclusively from `SecurityContextHolder.getContext().getAuthentication().getName()`.