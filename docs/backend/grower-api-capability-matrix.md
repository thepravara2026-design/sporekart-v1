# Grower API Capability Matrix — SPOREKART v3.0

| FD-14 Capability | Frontend Route | Current Backend API | Backend Route Exists | Server Security Role | Server Data Scoped | Contract Match | Production Status |
|------------------|----------------|---------------------|----------------------|----------------------|--------------------|----------------|-------------------|
| Grower Dashboard | `/grower` | `/api/v1/grower/dashboard` | Fallback / Admin APIs | `ROLE_ADMIN` | Global (Unscoped) | Partial | Fallback Synthesized |
| Grower Profile | `/grower/profile` | `/api/v1/auth/me` | Yes | `isAuthenticated()` | User-scoped | Partial | Generic Profile |
| Products List | `/grower/products` | `/api/v1/catalog/products` | Yes | Public | Global (Catalog) | Yes | Read-Only |
| Inventory List | `/grower/inventory` | `/api/v1/admin/inventory` | Yes | `ROLE_ADMIN` | Global (Unscoped) | Yes | Admin API Reuse |
| Stock Adjustment | Modal | `/api/v1/admin/inventory/{sku}/adjustments` | Yes | `ROLE_ADMIN` | Global (Unscoped) | Yes | Admin API Reuse |
| Order Visibility | `/grower/orders` | `/api/v1/admin/orders` | Yes | `ROLE_ADMIN` | Global (Unscoped) | Yes | Admin API Reuse |
| Order Transition | Table Action | `/api/v1/admin/orders/{id}/*` | Yes | `ROLE_ADMIN` | Global (Unscoped) | Yes | Admin API Reuse |
| Shipments | `/grower/shipments` | `/api/v1/admin/shipments` | Yes | `ROLE_ADMIN` | Global (Unscoped) | Yes | Admin API Reuse |
| Reports | `/grower/reports` | `/api/v1/admin/training-reports/summary` | Partial | `ROLE_ADMIN` | Global (Summary) | Partial | Summary Fallback |
| Settings | `/grower/settings` | `/api/v1/grower/settings` | No | N/A | Local | N/A | Local Storage |
