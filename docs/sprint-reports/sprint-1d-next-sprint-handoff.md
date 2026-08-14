# SPOREKART v3.0 — SPRINT 1D HANDOFF SPECIFICATION FOR SPRINT 1E

This document summarizes the verified frontend assets and contracts available for consumption in future sprints.

---

## 1. CURRENT FRONTEND ROUTES
- `/` — HomePage
- `/products` — ProductListPage (paginated grid, filter bar, sorting)
- `/products/:productId` — ProductDetailPage (UUID or SKU detail view)
- `/categories` — CategoryListPage (category browsing grid)
- `/health` — HealthPage (backend system health monitor)
- `*` — NotFoundPage (404 error boundary)

---

## 2. API CLIENT & QUERY ARCHITECTURE
- **API Client**: `catalogApi` (`src/services/catalogApi.ts`)
- **Endpoints**:
  - `catalogApi.getProducts(params, signal)`
  - `catalogApi.getProduct(productId, signal)`
  - `catalogApi.getCategories(params, signal)`
  - `catalogApi.getCategory(categoryId, signal)`
- **Query Hooks**: `useProducts`, `useProduct`, `useCategories`, `useCategory` in `src/features/catalog/hooks/useCatalog.ts`
- **Query Cache Keys**: `CATALOG_KEYS` (`['catalog', 'products']`, `['catalog', 'categories']`)

---

## 3. REUSABLE UI PRIMITIVES
- `ProductCard`: Renders product card with thumbnail, category pill, status badge, SKU, price, description, and view details action button.
- `ProductGrid`: Responsive CSS grid.
- `CatalogFilterBar`: Filter controls for search text, category selection, availability status, whitelist sort, and reset filters.
- `PaginationControls`: Server-side pagination controls with page counter and disabled bounds.

---

## 4. ENVIRONMENT & SETUP
- `VITE_API_BASE_URL`: Base URL for API requests.
- **DEV Profile**: `cd backend && mvn spring-boot:run` + `cd frontend && npm run dev`
- **Testing**: `npm run lint` + `npm run test` + `npm run build`

---

## 5. KNOWN LIMITATIONS & DEFERRED SCOPE
- Catalog browsing is currently read-only (cart, wishlist, authentication, payment, shipment, and admin management remain deferred).
