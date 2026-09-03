# SQ-03 — Commerce Domain Remediation & Root-Cause Analysis Report

## Root Cause Analysis & Technical Solutions

### 1. Catalog SKU Normalization Hardening (`Product.java`)
* **Problem**: `Product.normalizeSku(rawSku)` used `replaceAll("[^A-Z0-9-]", "")` to clean raw SKU strings. If an input string contained only special characters (e.g. `!@#$%`), regex stripping reduced the string to empty `""`, returning an invalid empty SKU.
* **Root Cause**: Missing post-strip validation guard.
* **Fix**: Added explicit check `if (normalized.isBlank())` throwing `IllegalArgumentException("SKU must contain at least one valid alphanumeric character or hyphen")`.

### 2. Category Slug Generation Protection (`Category.java`)
* **Problem**: `Category.generateSlug(input)` converted non-alphanumeric input strings into empty strings `""`.
* **Root Cause**: Lack of validation on the generated slug result.
* **Fix**: Added check `if (slug.isBlank())` throwing `IllegalArgumentException("Category name must contain at least one valid alphanumeric character for slug generation")`.

### 3. Cart Line Item Integer Overflow Safety (`CartItem.java`)
* **Problem**: `CartItem.incrementQuantity(addQuantity, maxQuantityPerItem)` calculated `targetQuantity = this.quantity + addQuantity`. If `addQuantity` was close to `Integer.MAX_VALUE`, integer overflow wrapped `targetQuantity` to a negative number, bypassing `maxQuantityPerItem` bounds checks.
* **Root Cause**: Unprotected 32-bit signed integer addition.
* **Fix**: Replaced raw addition with `Math.addExact(this.quantity, addQuantity)` wrapped in a try-catch block that throws `InvalidQuantityException("Quantity overflow detected")`.

### 4. Inventory Reservation Expiry Batch Reliability (`InventoryApplicationService.java`)
* **Problem**: In `expireReservationsBatch()`, if a stock reservation item contained a null SKU or requested releasing more stock than reserved, an exception in `releaseReservationInternal` would cause Spring's `@Transactional` context to mark the entire batch transaction `rollbackOnly`.
* **Root Cause**: Unbounded inventory release quantities and missing null checks for reservation items.
* **Fix**: Hardened `releaseReservationInternal` by ignoring null SKUs, checking `invItem.getReservedQuantity() > 0`, and capping `releaseQty = Math.min(resItem.getQuantity(), invItem.getReservedQuantity())`.

---

## Verification & Test Protection
Added [`CommerceDomainBoundaryTest.java`](file:///f:/sporekart-v3.0/backend/src/test/java/com/sporekart/modules/catalog/domain/CommerceDomainBoundaryTest.java) containing 6 targeted unit & integration tests covering SKU normalization edge cases, slug generation, cart overflow safety, and inventory available quantity calculation.
