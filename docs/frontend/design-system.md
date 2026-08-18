# FD-03 — Sporekart Frontend Design System Architecture

**Sprint:** FD-03  
**Repository:** `f:/sporekart-v3.0`  
**Date:** 2026-08-18  
**Status:** Complete  

---

## 1. Executive Summary

This document describes the design system implementation architecture for SPOREKART v3.0. It details how design tokens are structured in TypeScript (`src/design-system/tokens/`), bound into CSS Custom Properties (`src/index.css`), integrated into Tailwind CSS (`tailwind.config.js`), and made accessible and responsive across all viewports.

---

## 2. Design System Directory Architecture

```text
frontend/src/
├── design-system/
│   ├── tokens/
│   │   ├── colors.ts            # Primitive & semantic color tokens
│   │   ├── typography.ts        # Fonts, sizes, weights, line-heights
│   │   ├── spacing.ts           # 4px spacing scale & container widths
│   │   ├── radii.ts             # Border radius tokens
│   │   ├── shadows.ts           # Elevation & glow shadow tokens
│   │   ├── zIndex.ts            # Controlled z-index stacking hierarchy
│   │   ├── motion.ts            # Transitions, durations, easings
│   │   ├── breakpoints.ts       # Responsive viewport breakpoints
│   │   └── index.ts             # Master tokens export index
│   └── __tests__/
│       └── designSystem.test.ts # Automated unit tests for tokens & showcase
├── pages/
│   └── DesignSystemShowcase.tsx # Development-only visual validation showcase
├── index.css                    # Tailwind directives & CSS custom properties
├── index.html                   # Inter & Noto Sans Kannada Google Fonts
└── tailwind.config.js           # Tailwind theme extension mapping tokens
```

---

## 3. Usage Rules & Guidelines

### 3.1 Token-First Principle
Always prefer semantic CSS custom properties or Tailwind utility classes over hardcoded values:

```tsx
// ❌ BAD: Hardcoded color and arbitrary padding
<button style={{ backgroundColor: '#6366f1', padding: '15px 23px' }}>Click</button>

// ✅ GOOD: Design Token Semantic Styling
<button className="btn btn-primary">Click</button>
// OR
<button style={{ backgroundColor: 'var(--accent-primary)', padding: 'var(--space-4)' }}>Click</button>
```

### 3.2 Iconography Standard
All user interface icons MUST use `lucide-react` components:

```tsx
import { CheckCircle, AlertTriangle } from 'lucide-react';

<span className="badge badge-success">
  <CheckCircle size={14} /> Active
</span>
```

### 3.3 Keyboard Focus & Accessibility
All interactive controls accept keyboard focus cleanly and display a high-contrast bio-emerald focus ring (`:focus-visible`):

```css
:focus-visible {
  outline: 2px solid var(--forest-500);
  outline-offset: 2px;
}
```

---

## 4. Visual Validation Showcase Route

A development-only visual reference page has been wired into the React Router:
- **Route:** `/design-system-showcase`
- **Component:** [`DesignSystemShowcase.tsx`](file:///f:/sporekart-v3.0/frontend/src/pages/DesignSystemShowcase.tsx)
- **Features Demonstrated:** Color Swatches, Typography Scale, Kannada font rendering, Buttons, Status Badges, Focus Target Inputs, and Reduced Motion indicators.
