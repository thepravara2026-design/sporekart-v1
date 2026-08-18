# SPOREKART v3.0 — Accessibility Architecture & WCAG 2.2 AA Compliance Standard

**Sprint:** FD-04 & FD-05  
**Repository:** `f:/sporekart-v3.0`  
**Date:** 2026-08-18  
**Status:** Active Baseline  

---

## 1. Executive Summary

This document specifies the accessibility architecture and implementation guidelines for SPOREKART v3.0. The framework strictly adheres to WCAG 2.2 Level AA requirements, guaranteeing keyboard operability, screen reader landmark clarity, focus management, and non-visual state communication.

---

## 2. Accessibility Core Rules & Standards

### 2.1 Accessible HTML5 Landmarks
Every application page MUST render a clean landmark structure without duplicate unlabelled regions:
- **Banner:** `<header className="navbar" role="banner">`
- **Primary Navigation:** `<nav aria-label="Primary navigation">`
- **Main Content:** `<main id="main-content" tabIndex={-1}>`
- **Sidebar Navigation:** `<aside aria-label="Admin console sidebar navigation">`
- **Contentinfo (Footer):** `<footer role="contentinfo" aria-label="Site footer">`

### 2.2 Keyboard Skip Navigation
The `<SkipLink />` component is rendered as the first element in every layout. It is positioned offscreen (`top: -9999px`) until targeted by keyboard focus (`Tab`), at which point it shifts to `top: 1rem` with Bio-emerald background and skips directly to `#main-content`.

### 2.3 Mobile Navigation Drawer & Dialog Focus Management (FD-04 & FD-05)
- Trigger button uses `aria-expanded={isOpen}`, `aria-controls="mobile-nav-drawer"`, and explicit `aria-label`.
- Opening the modal dialog or drawer focuses the close button (`.dialog-close-btn` / `.drawer-close-btn`).
- Pressing `Escape` or clicking the backdrop closes the modal/drawer and restores keyboard focus to the trigger element (`triggerRef.current.focus()`).
- Background page scrolling is locked (`body.style.overflow = 'hidden'`) while modal/drawer is active.

### 2.4 Focus-Visible Rings
All focusable elements (`<a>`, `<button>`, `<input>`, `<select>`) exhibit a high-contrast 2px bio-emerald outline offset (`:focus-visible`):
```css
:focus-visible {
  outline: 2px solid var(--forest-500);
  outline-offset: 2px;
}
```

### 2.5 Form Controls & FormField ARIA Association (FD-05)
- Every `<FormField>` explicitly links `label` to control via `htmlFor` and matching `id`.
- Helper descriptions link to control via `aria-describedby="{id}-desc"`.
- Error messages link to control via `aria-describedby="{id}-error"` and render with `role="alert"`.
- Inputs expose `aria-invalid="true"` when validation fails.
- Icon-only buttons (`<IconButton />`) mandate an `aria-label` prop.

### 2.6 Interactive Primitives Keyboard Map (FD-05)
- **Dialog & Drawer:** `Tab` focus trap inside overlay, `Escape` key close.
- **DropdownMenu:** `ArrowDown` / `ArrowUp` item navigation, `Home` (first item), `End` (last item), `Enter` / `Space` select item, `Escape` close.
- **Tabs:** `role="tablist"`, `role="tab"`, `role="tabpanel"`, `aria-selected`. `ArrowLeft` / `ArrowRight` tab switching.
- **Switch:** `role="switch"`, `aria-checked`, `Space` / `Enter` toggle.
- **Toast Notifications:** Fixed container with `aria-live="polite"` (`role="status"`) for info/success and `aria-live="assertive"` (`role="alert"`) for errors.

### 2.7 Loading & Skeleton Semantics
- Active loading spinners use `role="status"` and explicit `aria-label="Loading..."` for screen readers.
- Purely visual skeleton placeholders use `aria-hidden="true"` to prevent screen reader noise during background data fetching.

### 2.8 Product Purchase Experience (FD-10)
- **Gallery:** Prev/next and zoom controls are real `<button>` elements with accessible names (`Previous image`, `Next image`, `Open image zoom`). Thumbnails expose `aria-label` and `aria-current` for the active slide; the thumbnail group supports `ArrowLeft` / `ArrowRight` / `Home` / `End` navigation, and `Escape` closes the zoom lightbox.
- **Quantity:** The stepper uses 40px touch targets (≥44px against adjacent interactive targets is not required here). The input exposes `aria-label="Quantity"`, `inputMode="numeric"`, `aria-invalid`, and `aria-describedby` linking to its error message.
- **Purchase Errors:** Inline mutation failures render inside the purchase panel with `role="alert"`; toast failures use `aria-live="assertive"`. The UI never announces success before the cart API resolves.
- **Image Fallback:** When no image URL exists in the product contract, the gallery renders a branded placeholder with `alt="Mushroom Spawn"` instead of a broken image.
- **Sticky Purchase Bar (mobile):** The sticky bar appears only on mobile viewports and respects safe-area padding; it does not trap focus or introduce overlay landmarks.

### 2.9 Checkout & Order Review Experience (FD-12)

- **Step Progress:** `CheckoutStepper` is a `<nav aria-label="Checkout progress">` with `<ol>`/`<li>` items; the current step is announced via `aria-current="step"`, completed steps render a check, and separators are `aria-hidden`. Steps are informational (navigation happens via form CTAs).
- **Address Form:** `FormField` label→`htmlFor`/`id` association, `aria-invalid` on failed controls, and `aria-describedby="{id}-error"` linking error messages that render with `role="alert"`. Client-side validation is convenience only; the backend remains authoritative for address acceptance.
- **Revalidation Notice & Warnings:** `CheckoutValidationAlert` renders blocking warnings (item unavailable) as `role="alert"` (assertive) and advisory/price warnings via the Alert's `role="status"` live region so screen readers are informed when the order total changes after review.
- **Place Order CTA:** The submit button sets `aria-busy` and an accessible `Loading...` name while the order pipeline runs; radios and the CTA are `disabled`, and the handler no-ops while a submission is pending (duplicate-submission protection).
- **Error State:** `CheckoutErrorState` shows a safe user-facing message (never stack traces) with the backend `requestId` as a diagnostic reference, plus Retry and Return to Cart actions.
- **Loading States:** `CheckoutSkeleton` placeholders use `aria-hidden="true"` skeletons (no screen-reader noise); explicit computing status text (`role="status"`) announces preview calculation.
