# FD-01 — Accessibility Baseline & WCAG 2.2 Audit

**Sprint:** FD-01  
**Repository:** `f:/sporekart-v3.0/frontend`  
**Date:** 2026-08-18  
**Status:** Completed  

---

## 1. Overview

This document presents the initial accessibility baseline audit of the SPOREKART v3.0 React application. The audit evaluates semantic HTML tags, keyboard navigation, focus indicator visibility, screen reader ARIA landmark attributes, form input label associations, color contrast ratios, and touch target dimensions against **WCAG 2.2 AA standards**.

---

## 2. Accessibility Audit Results Summary

| WCAG Criteria Category | Audit Item | Findings & Baseline Status | Remediation Roadmap Sprint |
| :--- | :--- | :--- | :--- |
| **Semantic HTML** | Landmarks | `<nav>`, `<main>`, `<footer>` present in [`MainLayout.tsx`](file:///f:/sporekart-v3.0/frontend/src/layouts/MainLayout.tsx). Missing `<aside>` and `<header>`. | FD-04 |
| **Keyboard Navigation**| Tab Traversal | All native `<Link>`, `<button>`, and `<input>` elements accept tab focus cleanly. | FD-04 / FD-05 |
| **Focus Visibility** | Focus Rings | Native browser default outline present; no custom high-contrast focus rings defined in `index.css`. | FD-04 / FD-05 |
| **Accessible Names** | Button / Link Labels | Text buttons labeled. Filter icons & modal close buttons missing `aria-label` or `sr-only` text. | FD-05 / FD-06 |
| **Form Controls** | Label Associations | Standard `<label htmlFor="...">` present on catalog search/filter controls. Unrouted admin modals missing `htmlFor` bindings. | FD-05 / FD-06 |
| **Error Associations** | `aria-invalid` / `aria-describedby` | Form validation errors rendered as plain text spans; missing explicit `aria-describedby` link to inputs. | FD-06 |
| **Modals & Overlays** | Focus Trapping / Escape key | Custom modals in admin pages lack keyboard focus trap and Escape key listener. | FD-06 |
| **Image Alternatives**| `alt` text | Product images in [`ProductCard.tsx`](file:///f:/sporekart-v3.0/frontend/src/features/catalog/components/ProductCard.tsx) include product title as `alt` text. | FD-07 |
| **Color Contrast** | Text vs Background | White `#f9fafb` on Dark `#0b0f19` yields 16.8:1 contrast (Pass). Secondary text `#9ca3af` on Dark yields 6.4:1 contrast (Pass). | FD-03 / FD-21 |
| **Touch Targets** | CTA Dimensions | Navigation links and buttons exceed minimum 44×44px touch target area on desktop viewports. | FD-20 / FD-21 |
| **Reduced Motion** | `prefers-reduced-motion` | CSS transitions (`0.2s ease`) do not currently respect `prefers-reduced-motion` media queries. | FD-04 / FD-21 |

---

## 3. Targeted Accessibility Remediation Strategy

- **FD-04 (Core Layout & Accessibility Foundation):** Add semantic `<header>`, `<main>`, `<aside>`, and `<footer>` landmarks, introduce high-contrast focus ring utility CSS, and implement `prefers-reduced-motion` reset rules.
- **FD-05 & FD-06 (Primitive & Overlay UI):** Mandate Radix UI primitives (`@radix-ui/react-dialog`, `@radix-ui/react-select`, `@radix-ui/react-dropdown-menu`) to guarantee automated keyboard focus trapping, `aria-expanded`, `aria-controls`, and Escape key dismissal out of the box.
- **FD-21 (Accessibility Hardening):** Perform comprehensive axe-core automated audits and manual screen reader testing (NVDA / VoiceOver) across all user flows.
