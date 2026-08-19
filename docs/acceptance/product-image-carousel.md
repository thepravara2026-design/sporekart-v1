# SPOREKART v3.0 — FE-PRODUCT-IMAGE-CAROUSEL-01 Acceptance Report

**Document ID:** `FE-PRODUCT-IMAGE-CAROUSEL-01`  
**Feature:** Multi-Image Product Card Auto-Sliding Carousel  
**Branch:** `feature/product-image-carousel`  
**Date:** 2026-08-19  
**Status:** Implemented & Verified

---

## 1. Executive Summary

Products can now carry up to four ordered images. Storefront product cards render
them as an auto-sliding, accessible carousel (default 3500 ms autoplay) with hover
pause, indicator dots, prev/next arrows, touch-swipe navigation, reduced-motion
support, and graceful fallback to the branded placeholder. The product detail page
gallery surfaces the same image set. Growers/admins can set and replace images
through the existing grower API. The change is fully backward compatible: products
without images render exactly as before.

## 2. Scope & Non-Regressions

- Backend domain/entity/DTO: `ProductImage`, `ProductImageEntity`, `ProductImageDto`.
- `Product`, `ProductEntity`, `ProductDto` extended with an ordered `images` list
  plus a convenience `imageUrl` (first image). Existing constructors preserved.
- Schema: new `product_images` table via Flyway `V45__product_images.sql`
  (`UNIQUE(product_id, display_order)`, index on `product_id`, FK cascade delete).
- Grower API: `CreateGrowerProductRequestDto.imageUrls` (create/update) and
  `PUT /api/v1/grower/products/{id}/images` (full replacement, max 4, ownership
  enforced; admin allowed for any product).
- Frontend: new `ProductImageCarousel` component, wired into `ProductCard` and the
  detail-page `ProductGallery`; `Product`/`GrowerProduct` types, zod contract
  schemas, and grower API mapping updated (all additions optional).
- No existing migration, table, business rule, or authorization was weakened.

## 3. Behavior Specification (as implemented)

- Autoplay interval: `PRODUCT_IMAGE_CAROUSEL_INTERVAL = 3500` ms (config constant).
- Max carousel images: `MAX_PRODUCT_IMAGES = 4` (frontend slices; backend rejects >4).
- Local state only (`activeImageIndex`, `isPaused`, `isHovered`); no global state.
- Timer created once per mounted carousel and cleaned up on unmount.
- Pauses on hover, focus, and touch; resumes on leave/blur/end.
- `prefers-reduced-motion: reduce` disables autoplay.
- Manual selection (arrow/indicator/swipe) continues from the chosen slide without
  restarting the autoplay timer aggressively.
- Control clicks `preventDefault`/`stopPropagation` so the card link never navigates;
  a horizontal swipe suppresses the following tap-through.
- Single image / empty set render statically with no controls or timers.
- Broken images fall back to the branded placeholder and are skipped by autoplay.
- Accessibility: `role="region"` + `aria-roledescription="carousel"`, labelled
  arrows/indicators, `aria-current`, polite live region for the active slide,
  `aria-hidden` on non-active slides, keyboard-focusable controls.

## 4. Verification Results

| Check | Result |
|-------|--------|
| Backend `mvn test` | **834 passed**, 0 failures, 0 errors (baseline 824 + 10 new) |
| New backend tests | `ProductImageCarouselPersistenceTest` (6), `GrowerSecurityAcceptanceTest` scenarios 9–12 (4) |
| Frontend `vitest` | **451 passed** (72 files; baseline 436 + 15 new carousel tests) |
| `npx tsc --noEmit` | 0 errors |
| `npm run lint` | 0 warnings, 0 errors |
| `npm run build` | Clean production build |

## 5. Files Changed / Added

Backend:
- `db/migration/V45__product_images.sql` (new)
- `catalog/domain/product/ProductImage.java` (new)
- `catalog/infrastructure/persistence/ProductImageEntity.java` (new)
- `catalog/application/ProductImageDto.java` (new)
- `catalog/domain/product/Product.java` (images list, `MAX_PRODUCT_IMAGES`)
- `catalog/infrastructure/persistence/ProductEntity.java` (images mapping)
- `catalog/infrastructure/persistence/ProductRepositoryImpl.java` (delete-before-save for constraint-safe replacement)
- `catalog/infrastructure/persistence/SpringDataProductRepository.java` (`deleteImagesByProductId`)
- `catalog/application/ProductDto.java` (`imageUrl`, `images`)
- `grower/web/dto/CreateGrowerProductRequestDto.java` (`imageUrls`)
- `grower/web/dto/UpdateProductImagesRequestDto.java` (new)
- `grower/application/GrowerApplicationService.java` (create/update images, `updateProductImages`)
- `grower/web/GrowerController.java` (`PUT /products/{id}/images`)
- Tests: `ProductImageCarouselPersistenceTest.java` (new), `GrowerSecurityAcceptanceTest.java`

Frontend:
- `catalog/components/ProductImageCarousel.tsx` (new)
- `catalog/components/ProductImageCarousel.test.tsx` (new, 15 tests)
- `catalog/components/ProductCard.tsx` (use carousel)
- `catalog/pages/ProductDetailPage.tsx` (pass images to gallery)
- `catalog/constants/catalogConstants.ts` (interval/max/swipe constants)
- `catalog/index.ts` (export carousel)
- `grower/components/GrowerProductCard.tsx` (image preview)
- `grower/types/growerProduct.ts`, `grower/api/growerApi.ts` (images)
- `types/catalog.ts` (`ProductImage`, `Product.images`/`imageUrl`)
- `types/schemas/contractSchemas.ts` (`ProductImageSchema`, optional `images`/`imageUrl`)
- `index.css` (carousel styles)

## 6. Acceptance Criteria

All FE-PRODUCT-IMAGE-CAROUSEL-01 acceptance criteria verified through unit tests
(carousel autoplay, pause, reduced motion, cleanup, control isolation), regression
suites, and code review. Non-breaking and backward compatible.