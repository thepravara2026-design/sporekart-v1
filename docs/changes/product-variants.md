# Feature Modification: Product Variants & Multi-Unit Quantity Support (`SPK-CHG-PRODUCT-VARIANTS-001`)

## Overview

SPOREKART v3.0 has been enhanced with hierarchical **Product Variants & Multi-Unit Quantity Support**. This modification enables a single catalog `Product` to offer multiple sellable size/quantity variations (e.g. `100 g`, `250 g`, `500 g`, `1 kg`, `100 ml`, `250 ml`, `500 ml`, `1 L`) with independent pricing, strike-out price validation, stock tracking, and SKU management.

---

## Key Changes

### 1. Database Schema (`V44__product_variants.sql`)
- Created `product_variants` table:
  - `id UUID PRIMARY KEY`
  - `product_id UUID REFERENCES products(id) ON DELETE CASCADE`
  - `sku VARCHAR(50) NOT NULL UNIQUE`
  - `quantity_value DECIMAL(10, 2) NOT NULL CHECK (quantity_value > 0)`
  - `quantity_unit VARCHAR(10) NOT NULL` (e.g. `G`, `KG`, `ML`, `L`)
  - `selling_price DECIMAL(12, 2) NOT NULL CHECK (selling_price >= 0)`
  - `strike_out_price DECIMAL(12, 2) DEFAULT NULL`
  - `status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'`
- Added constraints:
  - `CONSTRAINT chk_variant_strike_out_price CHECK (strike_out_price IS NULL OR strike_out_price > selling_price)`
  - `CONSTRAINT uq_product_variant_quantity UNIQUE (product_id, quantity_value, quantity_unit)`
- Zero-Downtime Data Migration:
  - Automatically populated default 1-unit variants for all existing products.
  - Linked existing `inventory_items` to the generated default variants.

### 2. Backend Domain & Architecture
- Added `QuantityUnit` enum: `G`, `KG`, `ML`, `L`.
- Created `ProductVariant` domain entity with domain validation and invariant checks.
- Enhanced `Product` domain model with `List<ProductVariant> variants`, variant-level duplicate validation, and dynamic minimum base price calculation.
- Mapped `@OneToMany` relationship in `ProductEntity` and `ProductVariantEntity`.
- Optimized `SpringDataProductRepository` with `LEFT JOIN FETCH p.variants` to eliminate N+1 queries.
- Updated `CatalogDataSeeder` to seed multi-variant products in `dev`/`qat` profiles.

### 3. Frontend Customer Storefront & Commerce
- Updated `frontend/src/types/catalog.ts` with `QuantityUnit` and `ProductVariant` models.
- **Single Storefront Product Card**: `ProductCard.tsx` renders ONE card per product with interactive variant size pills (`[100 g] [250 g] [500 g] [1 kg]`).
- Instantaneous reactive price, strike-out price, and stock status updates upon pill selection.
- Enhanced `ProductInfo.tsx` and `ProductActions.tsx` on product detail pages to select variants and add specific variants to cart (`variantId`).
- `cartApi.ts` updated to support `variantId`.

---

## Verification & Quality Baseline

| Suite | Status | Results |
|---|---|---|
| Backend Test Suite (`mvn test`) | **PASS** | 824 tests passed, 0 failures, 0 errors |
| Frontend Test Suite (`vitest`) | **PASS** | 70 test files passed, 430 tests passed |
| TypeScript Type Checking (`tsc`) | **PASS** | 0 errors (`npx tsc --noEmit`) |
| ESLint Code Quality (`lint`) | **PASS** | 0 warnings, 0 errors (`npm run lint`) |
| Production Build (`vite build`) | **PASS** | Clean production build completed |
