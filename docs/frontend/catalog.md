# SPOREKART v3.0 — FRONTEND CATALOG INTEGRATION SPECIFICATION

## Overview
This document specifies the frontend implementation of the Catalog Domain for Sporekart v3.0, integrated with the Spring Boot Catalog REST API delivered in Sprint 1C.

---

## 1. CATALOG ROUTES
- `/products`: Paginated product listing grid with filtering, searching, sorting, loading skeletons, empty states, and pagination controls.
- `/products/:productId`: Detailed product view (resolves by UUID or SKU), showing category, status, price, description, and metadata.
- `/categories`: Category browsing grid with search and direct navigation to category-filtered product listings.

---

## 2. API CLIENT & INTEGRATION
- **Base URL**: Configured via `VITE_API_BASE_URL` (defaults to empty string for Vite dev server proxy or full origin).
- **Service**: Centralized in `src/services/catalogApi.ts` using `axiosInstance`.
- **Endpoints**:
  - `GET /api/v1/catalog/products`: Product list with page, size, sort, categoryId, status, and search query parameters.
  - `GET /api/v1/catalog/products/{productId}`: Product detail lookup by UUID or SKU.
  - `GET /api/v1/catalog/categories`: Category list with page, size, sort, and search query parameters.
  - `GET /api/v1/catalog/categories/{categoryId}`: Category detail lookup by UUID or slug.

---

## 3. QUERY & STATE ARCHITECTURE
- Powered by `@tanstack/react-query` v5 (`useQuery`) via custom hooks in `src/features/catalog/hooks/useCatalog.ts`.
- Server state is cached with structured query keys (`CATALOG_KEYS`), supporting request cancellation via `AbortSignal`.
- URL state (`useSearchParams`) drives page index, page size, sorting, category filtering, availability status, and search terms across navigation and page refreshes.

---

## 4. UI COMPONENTS
- `ProductCard`: Product thumbnail card with category pill, status badge, title, SKU, description, price, and details button.
- `ProductGrid`: Responsive CSS grid container (`auto-fill, minmax(280px, 1fr)`).
- `CatalogFilterBar`: Integrated filter controls for searching, category selection, availability status, whitelist sorting, and clearing active filters.
- `PaginationControls`: Server-side pagination controls with page bounds indicator and previous/next navigation buttons.
- `CategoryListPage`: Dedicated category browsing view with search and direct product filtering links.

---

## 5. ACCESSIBILITY & RESPONSIVENESS
- Accessible HTML5 semantic markup with clear heading hierarchy (`h1`, `h2`, `h3`).
- Full keyboard navigation and visible focus rings.
- Fully responsive across Desktop (1200px+), Tablet (768px), and Mobile (<768px) viewports with flex-wrap and grid fallbacks.

---

## 6. TESTING & LOCAL DEVELOPMENT
- **Unit & Component Tests**: Run `npm test` in `frontend/` (Vitest + React Testing Library).
- **Lint**: Run `npm run lint` (`eslint --max-warnings 0`).
- **Production Build**: Run `npm run build` (`tsc && vite build`).
- **Full-Stack Execution**:
  ```bash
  # Terminal 1: Backend
  cd backend
  mvn spring-boot:run -Dspring-boot.run.profiles=dev

  # Terminal 2: Frontend
  cd frontend
  npm run dev
  ```
