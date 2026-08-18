# FD-04 — Application Shell & Layout Architecture

**Sprint:** FD-04  
**Repository:** `f:/sporekart-v3.0`  
**Date:** 2026-08-18  
**Status:** Active Baseline  

---

## 1. Executive Summary

This document specifies the core application shell and responsive layout architecture for SPOREKART v3.0. It defines the route layouts, responsive layout primitives (`Container`, `PageShell`, `PageSection`, `Stack`, `Inline`, `Grid`, `Sidebar`), header and mobile navigation, footer landmarks, error boundaries, and loading skeletons.

---

## 2. Application Shell Hierarchy

```text
Application Root (<App>)
 ├── Global Providers (QueryClient, ErrorBoundary, BrowserRouter)
 │
 └── Route-Level Layouts
      ├── MainLayout (Public & Customer Shell)
      │    ├── <SkipLink targetId="main-content" />
      │    ├── <Header /> (Banner landmark & primary nav)
      │    │    └── <MobileNav /> (Accessible drawer with focus management)
      │    ├── <main id="main-content"> (Main content region)
      │    │    └── <Outlet />
      │    └── <Footer /> (Contentinfo landmark)
      │
      ├── AdminLayout (Admin Console Shell)
      │    ├── <SkipLink targetId="admin-main-content" />
      │    ├── <Header />
      │    ├── <Sidebar title="Admin Console" items={ADMIN_NAVIGATION} />
      │    └── <main id="admin-main-content">
      │
      └── AuthenticatedLayout (Customer Portal Shell)
           ├── <SkipLink targetId="account-main-content" />
           ├── <Header />
           ├── <Sidebar title="My Portal" items={ACCOUNT_NAVIGATION} />
           └── <main id="account-main-content">
```

---

## 3. Reusable Layout Primitives (`src/components/layout/`)

| Primitive | Purpose | Key Props | FD-03 Tokens Consumed |
| :--- | :--- | :--- | :--- |
| `Container` | Centered max-width container with responsive padding | `maxWidth` (`sm`..`2xl`) | `layoutContainerWidths`, `spacing` |
| `PageShell` | Page header, title, subtitle, breadcrumbs & body wrapper | `title`, `subtitle`, `actions`, `breadcrumbs` | `fontSizes`, `spacing` |
| `PageSection` | Semantic `<section>` region with heading & accessibility label | `title`, `subtitle`, `ariaLabel` | `fontSizes`, `spacing` |
| `Stack` | Vertical flex layout | `gap` (1..24), `align` | `spacing` grid (4px base) |
| `Inline` | Horizontal wrapping flex layout | `gap` (1..24), `align`, `justify`, `wrap` | `spacing` grid (4px base) |
| `Grid` | Responsive multi-column auto-fill grid | `cols`, `minWidth`, `gap` | `spacing` grid (4px base) |
| `Sidebar` | Collapsible sidebar navigation container | `title`, `items`, `ariaLabel` | `radii`, `zIndex`, `motion` |

---

## 4. Navigation Architecture (`src/config/navigation.ts`)

Navigation structures are centralized to guarantee single-source-of-truth across desktop header, mobile drawer, and sidebars:

```typescript
export const MAIN_NAVIGATION: NavItem[] = [
  { label: 'Home', path: '/' },
  { label: 'Products', path: '/products' },
  { label: 'Categories', path: '/categories' },
  { label: 'System Health', path: '/health' },
  { label: 'Design Tokens', path: '/design-system-showcase' },
];
```
