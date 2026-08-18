# Grower Security & Threat Model Audit — SPOREKART v3.0

## Threat Model Assessment

### 1. Horizontal Privilege Escalation (Grower A → Grower B Data Access)
- **Status**: **VULNERABLE (P0)**
- **Finding**: Database tables (`products`, `inventory_items`, `orders`) do not contain a `grower_id` or `vendor_id` column. If a user is granted administrative access, queries return all products, stock quantities, and customer orders across all vendors platform-wide.
- **Remediation**: Add `grower_id` column to tables in Flyway migration `V42` and enforce `@Query("SELECT i FROM InventoryItemEntity i WHERE i.growerId = :growerId")` when caller role is `ROLE_GROWER`.

### 2. Vertical Privilege Escalation (Grower → Admin System Actions)
- **Status**: **MITIGATED IN FRONTEND / REQUIRES ROLE SEPARATION IN BACKEND (P0)**
- **Finding**: Currently, accessing `/api/v1/admin/*` requires `ROLE_ADMIN`. If a grower user is assigned `ROLE_ADMIN` to access inventory/order dispatches, they also inherit system-wide platform admin authorities (`ADMIN_ACCESS`, user management, batch management).
- **Remediation**: Define `ROLE_GROWER` in `UserRole.java` and `V42__grower_security_domain.sql` with dedicated authorities (`GROWER_READ_INVENTORY`, `GROWER_MANAGE_INVENTORY`, `GROWER_READ_ORDERS`, `GROWER_FULFILL_ORDER`).

### 3. Insecure Direct Object Reference (IDOR) on Orders & Inventory
- **Status**: **REQUIRES BACKEND SCOPING (P0)**
- **Finding**: `/api/v1/admin/inventory/{sku}/adjustments` accepts any valid SKU and modifies stock without checking if the SKU belongs to the caller's registered farm/business.
- **Remediation**: Validate product SKU ownership in `InventoryApplicationService.adjustStock()` before applying quantity updates.

### 4. Mass Assignment & JWT Role Tampering
- **Status**: **PROTECTED (PASS)**
- **Finding**: JWT tokens are signed server-side using HS256/RS256 keys. Role claims in JWT cannot be tampered with on the client.

### 5. Client-Side-Only Authorization Risk
- **Status**: **PASS (SERVER AUTHORIZED)**
- **Finding**: Spring Security enforces `@PreAuthorize` checks on all controller endpoints server-side. Frontend route guards are UX helpers only.
