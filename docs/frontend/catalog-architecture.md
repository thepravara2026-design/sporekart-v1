# SPOREKART v3.0 — Product Catalog Feature Architecture Specification

## 1. Executive Summary

Sprint **FD-06 — Feature Component Architecture & Product Catalog Refactoring** establishes the production-grade frontend feature architecture for SPOREKART v3.0's product catalog experience.

It refactors the legacy product list, category list, and product detail components into modular, highly accessible, responsive feature components under `src/features/catalog/`.

---

## 2. Catalog Feature Architecture

### 2.1 API & Contracts (`src/features/catalog/api/`)
- `catalogApi.ts`: Re-exports domain methods (`getProducts`, `getProduct`, `getCategories`, `getCategory`) with support for query parameters, abort signals, and strong TypeScript generics (`ApiResponse<PageResponse<Product>>`).

### 2.2 Domain Types (`src/features/catalog/types/`)
- `Product`: Complete entity schema including `id`, `sku`, `name`, `description`, `price`, `currency`, `status` (`ACTIVE` | `OUT_OF_STOCK` | `DRAFT` | `DISCONTINUED` | `ARCHIVED`), `category`, and timestamps.
- `Category`: Entity schema for category classifications (`ACTIVE` | `INACTIVE`).
- `CatalogFilterState`: Type definition for local and URL query filter states.

### 2.3 Feature Hooks (`src/features/catalog/hooks/`)
- `useProducts(params)`: Query hook powered by TanStack Query for product listing.
- `useProduct(productId)`: Query hook for retrieving single product details.
- `useCategories(params)`: Query hook for category listing.
- `useCatalogFilters(initialState)`: Filter state management hook with URL parameter synchronization.
- `useAddToCart` (FD-10): Add-to-cart mutation syncing the `['cart']` query cache (`setQueryData` + `invalidateQueries`) only on a resolved server response.
- `useStickyActionVisibility` (FD-10): Composes `useIsMobileViewport` (`matchMedia`) and `useElementInViewport` (`IntersectionObserver`) to drive the mobile sticky purchase bar.

### 2.4 Domain Components (`src/features/catalog/components/`)
| Component | Description | UI Primitives Consumed |
| :--- | :--- | :--- |
| `ProductCard` | Product card with image, status badge, title, SKU, price, View Details link, and Add to Cart action | `Card`, `Badge`, `Button`, `ProductPrice`, `ProductAvailability`, `ProductImage` |
| `ProductCardSkeleton` | Zero CLS loading placeholder for catalog cards | `Card`, `Skeleton` |
| `ProductGrid` | Responsive grid wrapper for product cards | `Grid` |
| `ProductList` | Stack layout variant for product cards | `Stack` |
| `ProductPrice` | Formatted price display with currency formatting | `formatPrice` utility |
| `ProductAvailability` | Status indicator badge with contextual icons | `Badge` |
| `CategoryCard` | Category summary card with product count link | `Card`, `Badge`, `Button` |
| `CategoryGrid` | Grid layout for category cards | `Grid` |
| `CatalogSearch` | Accessible search control with clear action | `Input` |
| `CatalogSort` | Select dropdown for sorting catalog products | `Select` |
| `CatalogFilters` | Comprehensive filter panel for category, price range, and status | `FormField`, `Input`, `Select`, `Button` |
| `CatalogToolbar` | Top toolbar combining search, sort, total counts, and filter actions | `CatalogSearch`, `CatalogSort` |
| `CatalogPagination` | Accessible pagination controls | `Pagination` |
| `CatalogEmptyState` | Empty state container with filter reset trigger | `EmptyState`, `Button` |
| `CatalogErrorState` | Accessible error banner with query retry trigger | `Alert`, `Button` |
| `ProductGallery` | Primary image + lazy thumbnails, prev/next, zoom lightbox, image fallback | `Dialog`, `ProductImage`, `IconButton` |
| `ProductQuantity` | Quantity stepper with 40px targets, max `50`, clamping | `IconButton`, `Input` |
| `ProductActions` | Purchase panel with controlled quantity, inline error region, pending lock | `Button`, `ProductQuantity`, `ProductAvailability`, `Alert` |
| `ProductStickyAction` | Mobile sticky Add to Cart bar with safe-area padding | `Button`, `ProductQuantity`, `ProductPrice` |

### 2.5 Feature Pages (`src/features/catalog/pages/`)
- `ProductListPage`: Main product catalog page wrapped in FD-04 `PageShell`, handling filter state, skeletons, error boundaries, empty states, and pagination.
- `ProductDetailPage`: Comprehensive product view featuring breadcrumb navigation, image gallery, status badge, SKU meta, price formatting, detailed description, and cart actions. Handles loading / not-found / error-retry / out-of-stock / pending states and wires the mobile `ProductStickyAction` bar.
- `CategoryListPage`: Category browsing page with category cards and search.

### 2.6 Purchase Constants & Utilities (FD-10)
- `catalogConstants.ts` → `DEFAULT_MAX_PRODUCT_QUANTITY = 50` (mirrors the backend maximum item quantity).
- `catalogUtils.ts` → `isProductPurchasable(status)` (only `ACTIVE` products are purchasable, matching the backend `CartPort` gate) and `getStatusLabel(status)`.

### 2.7 Cart Integration
Cart state is owned by the `['cart']` TanStack Query key. `useAddToCart` writes the server response into the cache after the mutation resolves; the frontend never claims success before backend confirmation. A dedicated cart page is scheduled for a later sprint — the header cart link continues to point to `/products`.

---

## 3. Verification & Quality Assurance

- **Vitest Unit & Integration Suite**: `26 / 26` test files passed (100%), `212 / 212` tests passed (100%), covering catalog listing, category browsing, and the FD-10 product detail & purchase experience (gallery, quantity, purchase actions, sticky bar, page states, and the end-to-end purchase integration flow).
- **TypeScript Compliance**: `npx tsc --noEmit` passed with 0 errors.
- **Production Build**: `npm run build` passed.
