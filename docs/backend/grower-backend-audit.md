# SPOREKART v3.0 — Grower Backend Support Audit

## Executive Summary

**Final Classification:**  
**C. GROWER FRONTEND SUPPORTED BY ADMIN APIs WITH SECURITY/ARCHITECTURE REVIEW REQUIRED** (and **E. GROWER SUPPORT EXISTS BUT IS ARCHITECTURALLY UNSAFE / MISSING SERVER-SIDE DATA ISOLATION**)

**Overall Status:**  
**CONDITIONAL PASS / ACTION REQUIRED FOR BACKEND GROWER DOMAIN**

SPOREKART v3.0's backend currently does **NOT** contain dedicated Grower domain entities, database tables, permissions, or API endpoints. 
FD-14 successfully implemented a production-grade Grower frontend portal (`/grower/*`), which interfaces with existing backend endpoints—primarily `/api/v1/auth/me`, `/api/v1/catalog/products`, `/api/v1/admin/inventory`, `/api/v1/admin/orders`, and `/api/v1/admin/shipments`—with fallback handling.

However, from a backend architecture and security perspective:
1. `UserRole` in the database (`V17__security_identity_hardening_domain.sql`) defines ONLY `ROLE_CUSTOMER` and `ROLE_ADMIN`. There is NO `ROLE_GROWER`.
2. The `/api/v1/admin/*` endpoints consumed by FD-14 require `ROLE_ADMIN` authentication. When accessed by an admin user, they return **platform-wide global data** (e.g. all inventory items, all customer orders across all vendors), rather than data scoped to an individual grower.
3. No database entity (`Product`, `InventoryItem`, `Order`, `Shipment`) contains a `grower_id`, `supplier_id`, or `vendor_id` foreign key for server-side tenancy scoping.

---

## 1. Backend Architecture

- **Architecture**: Modular monolith built on Spring Boot 3.4.2, Java 21, Spring Security (JWT), Spring Data JPA, Flyway DB migrations.
- **Bounded Contexts**: Cart, Catalog, Checkout, Customer, Inventory, Notification, Order, Payment, Returns, Review, Security, Shipment, Support, Training.
- **Database Migrations**: 41 Flyway SQL migrations (`V1` through `V41`).

---

## 2. Identity & Roles

- **Defined Enums (`UserRole.java`)**: `ROLE_CUSTOMER`, `ROLE_ADMIN`.
- **Database Seed (`V17__security_identity_hardening_domain.sql`)**: `ROLE_CUSTOMER`, `ROLE_ADMIN`.
- **Authority Mappings**:
  - `ROLE_CUSTOMER`: `READ_CATALOG`, `CREATE_ORDER`, `READ_OWN_ORDER`
  - `ROLE_ADMIN`: `READ_CATALOG`, `CREATE_ORDER`, `READ_OWN_ORDER`, `ADMIN_ACCESS`, `MANAGE_INVENTORY`
- **Grower Role**: Non-existent in backend security configuration or database tables.

---

## 3. Trainee vs Grower Model

- **Trainee Domain (`V31`–`V41`)**: Implemented. Covers mushroom cultivation training programs, batches, capacity slots, trainee enrollments, attendance, and certification verification (`ROLE_TRAINEE` / `ROLE_CUSTOMER` enrolled in training).
- **Grower Domain**: Not implemented in backend.
- **Separation**: Trainee and Grower are distinct concepts. Trainees participate in educational courses; Growers are commercial suppliers selling mycology products.
- **Current Relationship**: No database lifecycle or role transition exists between Trainee and Grower.

---

## 4. Grower Backend Components Found

- **Controllers**: 0 grower-specific controllers.
- **Services**: 0 grower-specific application/domain services.
- **Repositories**: 0 grower repositories.
- **Entities**: 0 grower entities.
- **Migrations**: 0 grower migrations.

---

## 5. Capability Matrix

| FD-14 Capability | Frontend API | Backend API | Backend Exists | Authorized | Scoped | Contract Match | Status |
|------------------|---------------|-------------|----------------|------------|--------|----------------|--------|
| Dashboard | `/api/v1/grower/dashboard` | Synthesized / `/api/v1/admin/*` | Partial | `ROLE_ADMIN` | Global | Partial | Fallback Synthesized |
| Profile | `/api/v1/auth/me` | `/api/v1/auth/me` | Yes | `isAuthenticated()` | User-scoped | Partial | Generic Profile Only |
| Products | `/api/v1/catalog/products` | `/api/v1/catalog/products` | Yes | Public | Global | Yes | Read-Only Catalog |
| Inventory | `/api/v1/admin/inventory` | `/api/v1/admin/inventory` | Yes | `ROLE_ADMIN` | Global | Yes | Unscoped Admin Inventory |
| Stock Adjustment | `/api/v1/admin/inventory/{sku}/adjustments` | `/api/v1/admin/inventory/{sku}/adjustments` | Yes | `ROLE_ADMIN` | Global | Yes | Unscoped Admin Adjust |
| Orders | `/api/v1/admin/orders` | `/api/v1/admin/orders` | Yes | `ROLE_ADMIN` | Global | Yes | Unscoped Admin Orders |
| Order Transition | `/api/v1/admin/orders/{id}/*` | `/api/v1/admin/orders/{id}/*` | Yes | `ROLE_ADMIN` | Global | Yes | Unscoped Admin Transition |
| Shipments | `/api/v1/admin/shipments` | `/api/v1/admin/shipments` | Yes | `ROLE_ADMIN` | Global | Yes | Unscoped Admin Shipments |
| Reports | `/api/v1/admin/training-reports/summary` | `/api/v1/admin/training-reports/summary` | Partial | `ROLE_ADMIN` | Global | Partial | Training/Platform Summary |
| Settings | `/api/v1/grower/settings` | Local / Fallback | No | N/A | Local | N/A | Frontend Local State |

---

## 6. Admin API Reuse Audit

- **`/api/v1/admin/inventory`**: Requires `ROLE_ADMIN`. Returns platform-wide inventory across all SKUs.
- **`/api/v1/admin/orders`**: Requires `ROLE_ADMIN`. Returns platform-wide customer orders.
- **`/api/v1/admin/shipments`**: Requires `ROLE_ADMIN`. Returns platform-wide shipments.

**Security Assessment:**  
Marked as **P0 — BLOCKING ARCHITECTURAL / SECURITY MISMATCH**. Using platform administrative endpoints for Grower operations provides un-scoped access to all platform inventory and customer orders if given `ROLE_ADMIN` privileges.

---

## 7. Database Support

- **Tables**: `users`, `roles`, `permissions`, `products`, `categories`, `inventory_items`, `orders`, `shipments`.
- **Ownership Columns**: 0 columns (`grower_id`, `supplier_id`, `vendor_id` are absent from `products`, `inventory_items`, and `orders`).

---

## 8. Authorization Model

Authorization is enforced via Spring Security `@PreAuthorize` annotations on controllers.
- Public endpoints (`/api/v1/catalog/products`) allow anonymous read.
- Customer endpoints (`/api/v1/orders`) enforce `isAuthenticated()` and filter by authenticated `customerId`.
- Admin endpoints (`/api/v1/admin/*`) enforce `hasAnyRole('ADMIN', 'ROLE_ADMIN')`.

---

## 9. Data Isolation Audit

- **Cross-Grower Isolation**: **FAIL (NOT IMPLEMENTED)**. Because products, inventory, and orders lack grower ownership identifiers in the database, server-side data isolation between distinct growers cannot currently be enforced.

---

## 10. API Contract Compatibility

- **Payloads**: Request/Response DTOs (`ProductDto`, `InventoryItemDto`, `OrderDto`, `ShipmentDto`, `PageResponse<T>`) match JSON structures cleanly.
- **Error Envelopes**: `ApiResponse<T>` matches across backend and frontend `apiClient`.

---

## 11. Security Findings

- **P0-1 (CRITICAL)**: **Missing Backend Grower Role & Permission Model**. `ROLE_GROWER` does not exist in backend security definitions or DB roles table.
- **P0-2 (CRITICAL)**: **Missing Multi-Tenant Grower Data Isolation**. Database schemas (`products`, `inventory_items`, `orders`) lack `grower_id` columns, preventing server-side data scoping.
- **P0-3 (HIGH)**: **Admin Endpoint Consumption**. Consuming `/api/v1/admin/*` endpoints for grower workflows grants platform-wide administrative visibility rather than tenant-isolated access.
- **P1-1 (MEDIUM)**: **Missing Grower Settings Persistence**. Operational settings (low-stock alert threshold, preferred carrier) are saved locally on frontend without backend DB storage.

---

## 12. Missing Backend Capabilities

1. `ROLE_GROWER` in `UserRole` enum and database migrations (`V42__grower_security_domain.sql`).
2. `grower_id` columns on `products`, `inventory_items`, and `orders` tables.
3. Dedicated Grower REST endpoints (`/api/v1/grower/dashboard`, `/api/v1/grower/products`, `/api/v1/grower/inventory`, `/api/v1/grower/orders`, `/api/v1/grower/reports`, `/api/v1/grower/settings`).
4. Grower business profile entity (`grower_profiles` table).

---

## 13. Existing Backend Capabilities Reusable by Grower

- `ProductApplicationService` and `CatalogProductController` (for product querying).
- `InventoryApplicationService` (stock adjustment and reservation logic).
- `OrderApplicationService` (order status transition logic).
- `ShipmentApplicationService` (shipment booking and tracking sync logic).
- `JwtTokenProvider` & `SecurityConfig` (for security filter extension).

---

## 14. Required Backend Work (Implementation Roadmap)

1. **Migration `V42`**: Add `ROLE_GROWER` role and `grower_profiles` table. Add nullable `grower_id` to `products`, `inventory_items`, and `orders`.
2. **Grower Domain Module**: Implement `com.sporekart.modules.grower` with `GrowerProfileEntity`, `GrowerApplicationService`, and `GrowerController`.
3. **Multi-Tenant Scoping**: Update queries in `ProductRepository`, `InventoryRepository`, and `OrderRepository` to enforce `WHERE grower_id = :growerId` when caller has `ROLE_GROWER`.
4. **Security Tests**: Add integration tests verifying Grower A cannot view or adjust Grower B's inventory/orders.

---

## 15. Test Coverage

- Backend unit & integration test suite: **100% Passed**.

---

## 16. Build Verification

- Backend compilation (`mvn test-compile`): **PASS**
- Backend test execution (`mvn test`): **PASS**

---

## 17. Final Decision

**CONDITIONAL PASS / ACTION REQUIRED FOR BACKEND GROWER DOMAIN**

FD-14 frontend implementation is complete, accessible, responsive, and robustly built with fallbacks. To promote FD-14 to production readiness, the backend requires the `V42` migration and multi-tenant `grower_id` data isolation scoping outlined in Section 14.
