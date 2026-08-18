# FD-01 — Design System Gap Analysis & Visual Baseline Audit

**Sprint:** FD-01  
**Repository:** `f:/sporekart-v3.0/frontend`  
**Date:** 2026-08-18  
**Status:** Completed  

---

## 1. Executive Summary

This document performs a detailed visual baseline audit comparing the current frontend styling implementation (`src/index.css`) against the authoritative **Sporekart Frontend Design System**. It outlines visual discrepancies across Color Tokens, Typography, Spacing Grid, Radius Tokens, Elevations/Shadows, Iconography, and Component Styling primitives.

---

## 2. Color System Reconciliation Matrix

| Palette Token | Intended Sporekart Design System Value | Current `index.css` Implementation | Gap Analysis / Action |
| :--- | :--- | :--- | :--- |
| **Primary Brand Background** | Dark Forest `#051c14` / `#0a291d` | `#0b0f19` (Slate Dark) | **GAP** — Convert to Forest Dark in FD-03 |
| **Secondary Container Background**| Emerald Tinted Dark `#0f382a` | `#111827` (Gray Dark) | **GAP** — Convert to Dark Emerald in FD-03 |
| **Card / Surface Background** | Glassmorphic Dark `#134233` with blur | `rgba(17, 24, 39, 0.7)` | **GAP** — Retain glassmorphism, adjust hue to Forest in FD-03 |
| **Accent Primary** | Spore Emerald `#10b981` / `#059669` | `#6366f1` (Indigo Blue) | **GAP** — Shift primary accent from Indigo to Emerald in FD-03 |
| **Accent Hover** | Deep Emerald `#047857` | `#4f46e5` (Deep Indigo) | **GAP** — Shift hover accent to Deep Emerald in FD-03 |
| **Text Primary** | Crisp White `#f9fafb` | `#f9fafb` | **MATCH** — Excellent contrast |
| **Text Secondary** | Muted Sage / Gray `#9ca3af` | `#9ca3af` | **MATCH** — Readable secondary text |
| **Success Color** | Bio-Green `#10b981` | `#10b981` | **MATCH** — Matches green status badge |
| **Danger Color** | Crimson Red `#ef4444` | `#ef4444` | **MATCH** — Matches danger status badge |

---

## 3. Typography & Font Baseline Audit

- **Current Font Declaration:** `font-family: 'Inter', system-ui, -apple-system, sans-serif;` in [`index.css`](file:///f:/sporekart-v3.0/frontend/src/index.css#L12).
- **Google Fonts Loading:** Currently relying on system fallbacks; no explicit Google Fonts `<link>` or `@import` in [`index.html`](file:///f:/sporekart-v3.0/frontend/index.html).
- **Kannada Font Support:** **Missing**. The design system requires `Noto Sans Kannada` for regional locale support.
- **Typography Tokens:** Heading styles use ad-hoc linear gradients (`linear-gradient(135deg, #818cf8, #c084fc)`). Typography sizes need standardization via Tailwind text utility classes (`text-xs`, `text-sm`, `text-base`, `text-lg`, `text-xl`, `text-2xl`, `text-3xl`, `text-4xl`).

---

## 4. Spacing, Radius, and Shadow Tokens Audit

### 4.1 Spacing Grid
- **Intended Baseline:** 4px base spacing scale (`0.25rem`, `0.5rem`, `0.75rem`, `1rem`, `1.5rem`, `2rem`, `3rem`).
- **Current Baseline:** Mixed margins and paddings (`padding: 2rem`, `padding: 1.25rem`, `padding: 0.4rem 0.85rem`). Will be unified when adopting Tailwind standard scale (`p-1`, `p-2`, `p-4`, `p-6`, `p-8`).

### 4.2 Border Radii
- **Current Declarations:**
  - Cards: `border-radius: 12px;`
  - Badges: `border-radius: 9999px;` (Pill shape)
  - Buttons: `border-radius: 8px;`
- **Target Alignment:** Standardize on Tailwind radius tokens (`rounded-md`, `rounded-lg`, `rounded-xl`, `rounded-full`) in FD-03.

### 4.3 Elevations & Shadows
- **Current Declarations:** `box-shadow: 0 8px 32px 0 rgba(0, 0, 0, 0.37);` (Heavy dark shadow).
- **Target Alignment:** Standardize on clean Tailwind elevation scales (`shadow-sm`, `shadow-md`, `shadow-lg`, `shadow-emerald-900/20`) in FD-03.

---

## 5. Iconography Audit

- **Current State:** Plain text (`"★"`, `"&times;"`, `"+ Add"`) or raw inline SVG icons.
- **Target Baseline:** **Lucide Icons** (`lucide-react`).
- **Action Plan:** Install `lucide-react` in FD-03 / FD-05 to replace all plain text indicators and inline icons across cards, buttons, nav headers, and status badges.

---

## 6. Visual Component Gap Analysis Summary

| Visual Area | Current Baseline State | Target Design System State | Remediation Sprint |
| :--- | :--- | :--- | :--- |
| **Global Theme** | Indigo/Purple Dark Theme | Sporekart Forest Dark / Emerald Theme | FD-03 |
| **Navigation Header** | Fixed navbar with text links | Glassmorphic Header + Mobile Drawer + Cart Trigger | FD-04 / FD-08 |
| **Product Cards** | Basic bordered container | Elevated Forest Card with image ratio, badge, rating, & price | FD-07 / FD-09 |
| **Training Cards** | Raw text table / console | Visual Batch Card with seats progress bar & date pills | FD-13 |
| **Form Controls** | Basic CSS inputs | Standardized Input, Select, Checkbox with focus ring & error text | FD-05 / FD-06 |
| **Modals & Overlays** | Raw custom fixed divs | Radix / shadcn Accessible Dialog with overlay blur | FD-06 |
