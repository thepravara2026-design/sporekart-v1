# SPOREKART v3.0 — Product Detail & Purchase Experience (FD-10)

**Sprint:** FD-10 — Product Detail & Product Purchase Experience
**Repository:** `f:/sporekart-v3.0`
**Date:** 2026-08-18
**Status:** Complete

---

## 1. Executive Summary

FD-10 delivers the production-grade product detail and purchase experience on top of the FD-06 catalog feature architecture. It covers the product gallery (with image fallback and lightbox), quantity selection, add-to-cart with cart-cache synchronization, a mobile sticky purchase bar, and complete loading / not-found / error / out-of-stock page states.

The implementation strictly respects the backend catalog contract: products expose **no image URLs, no variants, and no stock quantities**. The frontend therefore renders a branded placeholder in the gallery and does not fabricate variants, attributes, or inventory data. Only `ACTIVE` products are purchasable, mirroring the backend `CartPort` / `CatalogAdapter` behaviour.

---

## 2. Purchase Contract & Business Rules

### 2.1 Backend Contract (verified against `CatalogAdapter` / `CartService`)
- `ProductDto`: `id`, `sku`, `name`, `description`, `price`, `currency`, `status`, `category`, `createdAt`, `updatedAt`. **No images, no variants, no stock quantity, no attributes.**
- Purchasable status: `ACTIVE` only. Non-ACTIVE products (e.g. `OUT_OF_STOCK`, `DRAFT`, `DISCONTINUED`, `ARCHIVED`) are **not** purchasable.
- Cart API (`POST /cart/items`) requires authentication; unauthenticated requests receive `UNAUTHORIZED` (401).
- Invalid quantity → `CART_INVALID_QUANTITY` (400).
- Non-purchasable product → `CATALOG_PRODUCT_NOT_PURCHASABLE` (400).
- Maximum item quantity defaults to **50** on the backend.

### 2.2 Frontend Business Rules
| Rule | Source | Enforcement |
| :--- | :--- | :--- |
| Max purchasable quantity | Backend max `50` | `DEFAULT_MAX_PRODUCT_QUANTITY = 50` in `catalogConstants.ts` |
| Purchasability | Backend status gate | `isProductPurchasable(status)` in `catalogUtils.ts` |
| No images in contract | `ProductDto` | `ProductImage` renders branded placeholder when no `src` is supplied |
| Cart requires auth | Backend 401 | `useAddToCart` surfaces `UNAUTHORIZED` inline, never claims success |
| Success only on API confirmation | Idempotency guarantee | Cart cache updated only via `setQueryData(['cart'], response)` on resolved mutation |

---

## 3. Component Architecture

### 3.1 Components (`src/features/catalog/components/`)

| Component | Responsibility |
| :--- | :--- |
| `ProductGallery` | Primary image + lazy thumbnail strip; prev/next controls (only when >1 image); opens a zoom lightbox `Dialog`; keyboard navigation (`Arrow` / `Home` / `End`) on the thumbnail group; `ProductImage` fallback on error |
| `ProductQuantity` | Stepper with 40px icon-button targets; default max `50`; clamps to `[min, max]` (zero / negative / `NaN` normalized); `inputMode="numeric"`, accessible label `Quantity` |
| `ProductActions` | Controlled `quantity` / `onQuantityChange` / `mutation` props (backwards compatible); inline `role="alert"` error region (`add-to-cart-error`); disabled + pending state prevents duplicate submissions; honest status label when the product is not purchasable |
| `ProductStickyAction` | Mobile sticky Add to Cart bar (safe-area padding, backdrop blur) driven by `useStickyActionVisibility` |
| `ProductInfo` | Composes title, status, SKU, price, availability, description, category and purchase actions |
| `ProductMetadata` | Reads `getStatusLabel(status)` instead of exposing the raw enum |
| `ProductDescription` | Heading `Product Description`; no fabricated cultivation claims |
| `ProductPrice` | Default currency `INR` matching `formatPrice` |

### 3.2 Hooks (`src/features/catalog/hooks/`)
| Hook | Responsibility |
| :--- | :--- |
| `useAddToCart` | Mutation calling `cartApi.addItem`; on success `setQueryData(['cart'], response)` + `invalidateQueries(['cart'])`; `ApiError`-aware error mapping to inline region + toast |
| `useIsMobileViewport` | `matchMedia` guard (fails safe to false) |
| `useElementInViewport` | `IntersectionObserver` guard (fails safe to in-view) |
| `useStickyActionVisibility` | Composes the above; keeps the sticky bar visible until the purchase panel scrolls out of view |

### 3.3 Page States (`src/features/catalog/pages/ProductDetailPage.tsx`)
`product-detail-loading` → skeleton · `product-not-found` (404) · `product-detail-error` + Retry · success (`product-detail-card`) · out-of-stock (purchase disabled) · pending (Add to Cart locked during mutation). Layout uses the `.product-detail-layout` responsive grid (1 column mobile, 2 columns ≥768px) with `minmax(0, 1fr)` to prevent horizontal overflow at 320px.

---

## 4. Cart Cache Synchronization

The `['cart']` TanStack Query key is the single source of truth for cart state (the cart page is a later sprint; the header cart link still points to `/products`). `useAddToCart` writes the server response into the cache only after the mutation resolves, guaranteeing the UI never claims success before the backend confirms.

---

## 5. Accessibility (FD-10)

- Gallery controls are real `<button>` elements with accessible names (`Previous image`, `Next image`, `Open image zoom`); thumbnails have `aria-label` and `aria-current` for the active slide.
- Quantity controls expose `aria-label="Quantity"`, `aria-invalid`, and `aria-describedby` on error.
- Inline mutation errors render inside the purchase panel with `role="alert"`; toast failures use `aria-live="assertive"`.
- Touch targets are ≥40px on the quantity stepper (see `design-system.md`).
- Image fallback provides the `alt` "Mushroom Spawn" brand mark when no image URL exists.

---

## 6. Verification & Quality Assurance

- **Vitest Unit & Integration Suite:** `26 / 26` test files passed (100%), `212 / 212` tests passed (100%), including `ProductGallery`, `ProductQuantity`, `ProductActions`, `ProductStickyAction`, `ProductDetailPage`, and the end-to-end `ProductPurchaseIntegration` flow.
- **TypeScript Compliance:** `npx tsc --noEmit` passed with 0 errors.
- **Production Build:** `npm run build` passed.
- **Lint:** No FD-10 errors (pre-existing `admin` / `trainee` lint errors remain out of scope for this sprint).