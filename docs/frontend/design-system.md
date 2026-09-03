# FD-03 & FD-05 — Sporekart Frontend Design System & Shared UI Components

**Sprint:** FD-03 & FD-05  
**Repository:** `f:/sporekart-v3.0`  
**Date:** 2026-08-18  
**Status:** Complete  

---

## 1. Executive Summary

This document describes the design system and shared UI component implementation architecture for SPOREKART v3.0. It details how design tokens are structured in TypeScript (`src/design-system/tokens/`), bound into CSS Custom Properties (`src/index.css`), integrated into Tailwind CSS (`tailwind.config.js`), and exposed as production-grade shared UI primitives (`src/components/ui/`).

---

## 2. Design System & Component Directory Architecture

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
├── components/
│   └── ui/
│       ├── Button.tsx           # Button variant system
│       ├── IconButton.tsx       # Icon-only accessible button
│       ├── Input.tsx            # Form text/number/email input
│       ├── Textarea.tsx         # Multi-line text control
│       ├── Select.tsx           # Native select control wrapper
│       ├── Checkbox.tsx         # Accessible checkbox control
│       ├── RadioGroup.tsx       # Radio group selection control
│       ├── Switch.tsx           # Binary toggle switch control
│       ├── FormField.tsx        # Label, description, error wrapper
│       ├── Card.tsx             # Composable card primitives
│       ├── Badge.tsx            # Status indicator badge
│       ├── Alert.tsx            # Accessible status/error alert banner
│       ├── Dialog.tsx           # Modal dialog with focus trap
│       ├── Drawer.tsx           # Off-canvas drawer panel
│       ├── DropdownMenu.tsx     # Keyboard navigable menu
│       ├── Tooltip.tsx          # Accessible tooltip
│       ├── Tabs.tsx             # Accessible tablist and tabpanels
│       ├── Breadcrumb.tsx       # Semantic breadcrumb navigation
│       ├── Pagination.tsx       # Page navigation control
│       ├── Toast.tsx            # Fixed toast notification system
│       ├── EmptyState.tsx       # Domain-neutral empty container
│       ├── LoadingSpinner.tsx   # Loading spinner primitive
│       ├── Skeleton.tsx         # Skeleton loader primitive
│       └── index.ts             # Unified UI components export index
├── pages/
│   └── DesignSystemShowcase.tsx # Component and token visual showcase page
├── index.css                    # Tailwind directives & CSS custom properties
├── index.html                   # Inter & Noto Sans Kannada Google Fonts
└── tailwind.config.js           # Tailwind theme extension mapping tokens
```

---

## 3. Usage Rules & Guidelines

### 3.1 Token-First Principle
Always prefer semantic CSS custom properties, shared UI components (`src/components/ui/`), or Tailwind utility classes over hardcoded values:

```tsx
// ❌ BAD: Hardcoded color and arbitrary padding
<button style={{ backgroundColor: '#6366f1', padding: '15px 23px' }}>Click</button>

// ✅ GOOD: Shared UI Component with Design Token Styling
import { Button } from '../components/ui';
<Button variant="primary" size="md">Click</Button>
```

### 3.2 Iconography Standard
All user interface icons MUST use `lucide-react` components:

```tsx
import { CheckCircle, AlertTriangle } from 'lucide-react';
import { Badge } from '../components/ui';

<Badge variant="success" icon={<CheckCircle size={14} />}>Active</Badge>
```

### 3.3 Keyboard Focus & Accessibility
All interactive controls accept keyboard focus cleanly and display a high-contrast bio-emerald focus ring (`:focus-visible`):

```css
:focus-visible {
  outline: 2px solid var(--forest-500);
  outline-offset: 2px;
}
```

### 3.4 Quantity Stepper Touch Targets (FD-10)
The product quantity stepper (`ProductQuantity`) mandates **≥40px** icon-button targets to satisfy WCAG 2.2 Level AA pointer target sizing on the product detail page and the mobile sticky purchase bar.

### 3.5 Product Detail Layout (FD-10)
The product detail page uses the shared `.product-detail-layout` CSS class: a responsive grid that collapses to a single column on mobile and a two-column layout at ≥768px, with `minmax(0, 1fr)` tracks to prevent horizontal overflow at 320px viewports.

---

## 4. Visual Validation Showcase Route

A development-only visual reference page has been wired into the React Router:
- **Route:** `/design-system-showcase`
- **Component:** [`DesignSystemShowcase.tsx`](file:///f:/sporekart-v3.0/frontend/src/pages/DesignSystemShowcase.tsx)
- **Features Demonstrated:** Color Swatches, Typography Scale, Kannada font rendering, Buttons, IconButtons, Form Controls, Cards, Status Badges, Alerts, Dialogs, Drawers, DropdownMenus, Tooltips, Tabs, Breadcrumbs, Pagination, Toast Notifications, and Empty States.
