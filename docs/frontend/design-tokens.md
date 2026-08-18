# FD-03 — Master Design Tokens Specification

**Sprint:** FD-03  
**Repository:** `f:/sporekart-v3.0`  
**Date:** 2026-08-18  
**Status:** Active Baseline  

---

## 1. Overview

This document specifies the authoritative design token architecture for SPOREKART v3.0. It defines the primitive, semantic, and component-level tokens governing colors, typography, spacing, border radii, elevations/shadows, z-index layering, breakpoints, and motion transitions.

---

## 2. Token Architecture Layers

```text
Layer 1 — Primitive Tokens (Raw Values)
   ↓
Layer 2 — Semantic Tokens (Application Meanings)
   ↓
Layer 3 — Component Tokens (Component Styles / Tailwind Utility Classes)
```

---

## 3. Color Tokens Specification

### 3.1 Primitive Forest Palette (`primitiveColors.forest`)
- `forest.50`: `#ecfdf5`
- `forest.100`: `#d1fae5`
- `forest.200`: `#a7f3d0`
- `forest.300`: `#6ee7b7`
- `forest.400`: `#34d399`
- `forest.500`: `#10b981` (Bio Emerald Primary Accent)
- `forest.600`: `#059669` (Deep Emerald Hover)
- `forest.700`: `#047857`
- `forest.800`: `#134233` (Glassmorphic Card Surface)
- `forest.850`: `#0f382a` (Surface Container)
- `forest.900`: `#0a291d` (Dark Emerald Container)
- `forest.950`: `#051c14` (Sporekart Primary Background Dark)

### 3.2 Semantic Theme Color Mapping

| Semantic Token Name | CSS Custom Property | Raw Value / Mapping | Usage Guidance |
| :--- | :--- | :--- | :--- |
| `background.primary` | `--bg-primary` | `#051c14` | Main page background |
| `background.secondary` | `--bg-secondary` | `#0a291d` | Navbar / Section containers |
| `background.surface` | `--bg-surface` | `#0f382a` | Secondary containers |
| `background.card` | `--bg-card` | `rgba(19, 66, 51, 0.75)` | Product & feature cards |
| `text.primary` | `--text-primary` | `#f9fafb` | Primary headings & body text |
| `text.secondary` | `--text-secondary` | `#9ca3af` | Subtitles & secondary labels |
| `text.muted` | `--text-muted` | `#6b7280` | Disabled / footer text |
| `action.primary` | `--accent-primary` | `#10b981` | Primary CTA buttons & active indicators |
| `action.primaryHover` | `--accent-hover` | `#059669` | Primary CTA hover state |
| `status.success` | `--success-color` | `#10b981` | Success badges, order confirmation |
| `status.warning` | `--warning-color` | `#f59e0b` | Low stock badges, warnings |
| `status.error` | `--danger-color` | `#ef4444` | Out of stock, error alerts |
| `status.info` | `--info-color` | `#3b82f6` | System notifications |

---

## 4. Typography Specification

### 4.1 Font Families
- **Primary Sans:** `'Inter', 'Noto Sans Kannada', system-ui, -apple-system, sans-serif`
- **Regional Locale (Kannada):** `'Noto Sans Kannada', sans-serif`
- **Monospace:** `ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace`

### 4.2 Font Scale

| Token Name | Size (rem / px) | Line Height | Usage |
| :--- | :--- | :--- | :--- |
| `fontSizes.xs` | `0.75rem` (12px) | `1rem` | Captions, badges, timestamps |
| `fontSizes.sm` | `0.875rem` (14px) | `1.25rem` | Form labels, small buttons |
| `fontSizes.base` | `1rem` (16px) | `1.5rem` | Standard body text |
| `fontSizes.lg` | `1.125rem` (18px) | `1.75rem` | Lead paragraphs, card titles |
| `fontSizes.xl` | `1.25rem` (20px) | `1.75rem` | H4 headings, modal titles |
| `fontSizes.2xl` | `1.5rem` (24px) | `2rem` | H3 headings |
| `fontSizes.3xl` | `1.875rem` (30px) | `2.25rem` | H2 headings |
| `fontSizes.4xl` | `2.25rem` (36px) | `2.5rem` | H1 page titles |
| `fontSizes.5xl` | `3rem` (48px) | `1` | Hero display text |

---

## 5. Spacing Scale (4px Base Grid)

```text
space.1  = 0.25rem (4px)
space.2  = 0.50rem (8px)
space.3  = 0.75rem (12px)
space.4  = 1.00rem (16px)
space.6  = 1.50rem (24px)
space.8  = 2.00rem (32px)
space.12 = 3.00rem (48px)
space.16 = 4.00rem (64px)
```

---

## 6. Border Radii Scale

```text
radii.none = 0px
radii.sm   = 0.25rem (4px)   - Tooltips, small inputs
radii.md   = 0.50rem (8px)   - Buttons, standard inputs
radii.lg   = 0.75rem (12px)  - Product cards, filter containers
radii.xl   = 1.00rem (16px)  - Modals, large surfaces
radii.full = 9999px          - Pill badges, avatars
```

---

## 7. Elevation & Shadows Scale

```text
shadows.sm   = 0 1px 2px 0 rgba(0, 0, 0, 0.2)
shadows.md   = 0 4px 6px -1px rgba(0, 0, 0, 0.3)
shadows.lg   = 0 10px 15px -3px rgba(0, 0, 0, 0.4)
shadows.glow = 0 0 20px 0 rgba(16, 185, 129, 0.35) (Bio-emerald glow)
```

---

## 8. Z-Index Layering Scale

```text
zIndex.base     = 0   - Standard content
zIndex.dropdown = 10  - Select dropdowns
zIndex.sticky   = 20  - Sticky header navbar
zIndex.overlay  = 30  - Modal backdrop overlay
zIndex.modal    = 40  - Dialog modals
zIndex.toast    = 50  - Toast notifications
zIndex.tooltip  = 60  - Tooltips
```

---

## 9. Motion & Transitions

- `durations.fast`: `150ms` (Hover, state toggles)
- `durations.normal`: `250ms` (Modals, drawers)
- `durations.slow`: `350ms` (Page transitions)
- `easings.standard`: `cubic-bezier(0.4, 0, 0.2, 1)`
- **Accessibility:** `@media (prefers-reduced-motion: reduce)` resets duration to `0.01ms` to eliminate motion-induced vestibular discomfort.
