# Grower Experience UI/UX Guidelines — SPOREKART v3.0

## Design System & Tokens
The Grower Experience visually adheres to SPOREKART's core design system:
- **Background**: Forest Dark `#091510`
- **Surface Panels**: Deep Emerald `#0d231a`
- **Primary Accent**: Bio Emerald `#10b981`
- **Typography**: Inter / System sans-serif with accessible line heights
- **Status Colors**:
  - Success: `#34d399` (Active, Healthy, Completed, Delivered)
  - Warning: `#fbbf24` (Low Stock, Draft, Pending Payment)
  - Danger: `#f87171` (Out of Stock, Archived, Cancelled)
  - Info / Primary: `#60a5fa` (Processing, Ready for Fulfillment, Shipped)

## Component Reuse
The module reuses standard shared primitives from FD-03 to FD-05:
- `Card`, `Badge`, `Button`, `Input`, `Textarea`, `Switch`, `Dialog`, `Alert`, `EmptyState`, `Skeleton`, `LoadingSpinner`, `Toast`.
- Layout primitives from FD-04 (`Header`, `Sidebar`, `SkipLink`, `Footer`).

## Responsive Breakpoints
- **Mobile (<768px)**: Metric cards stack in a single column, tables offer horizontal scrolling container, sidebar collapses into touch-friendly navigation drawer.
- **Tablet (768px - 1024px)**: 2-column grid layout for metrics and product cards.
- **Desktop (>1024px)**: Full multi-column dashboard grid with sticky sidebar navigation.

## Accessibility (WCAG 2.2 AA)
- High contrast ratios across dark mode surfaces.
- All status badges combine visual color tokens with explicit text labels.
- Skip link (`#grower-main-content`) provided for keyboard users.
- Modal focus trapping and keyboard Escape handling in `GrowerStockAdjustDialog`.
