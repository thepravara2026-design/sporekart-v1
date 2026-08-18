# Grower Experience Architecture — SPOREKART v3.0

## Overview
The Grower Experience module (`frontend/src/features/grower/`) provides a scalable portal for growers, suppliers, and vendors to manage their operational activities in SPOREKART v3.0.

It integrates directly with the authoritative backend services, including Catalog, Inventory, Orders, Shipments, Returns, and User Identity endpoints.

## Feature Structure
```
features/grower/
├── api/
│   └── growerApi.ts               # Authoritative backend API client integrations & fallbacks
├── types/
│   ├── grower.ts                  # Core profile, dashboard metrics & operational types
│   ├── growerProduct.ts           # Product catalog management types
│   ├── growerInventory.ts         # Inventory on-hand, reservation & stock movement types
│   ├── growerOrder.ts             # Order pipeline & transition types
│   └── growerReport.ts            # Sales analytics & report summary types
├── constants/
│   └── growerConstants.ts         # Status configurations & query key definitions
├── utils/
│   └── growerUtils.ts             # Status badge mappers, currency & stock health helpers
├── schemas/
│   └── growerSchemas.ts           # Zod validation schemas for forms and commands
├── hooks/
│   ├── useGrowerDashboard.ts      # TanStack Query hook for dashboard metrics
│   ├── useGrowerProfile.ts        # User profile query & mutation hook
│   ├── useGrowerProducts.ts       # Product catalog query & filter hook
│   ├── useGrowerInventory.ts      # Inventory query & stock adjustment mutation hook
│   ├── useGrowerOrders.ts         # Order pipeline query & transition hook
│   ├── useGrowerShipments.ts      # Shipment tracking query hook
│   ├── useGrowerReports.ts        # Performance analytics hook
│   └── useGrowerSettings.ts       # Operational settings hook
├── components/
│   ├── GrowerLayout.tsx           # Dedicated portal shell layout
│   ├── GrowerMetricCard.tsx       # Standardized operational metric display widget
│   ├── GrowerStatusBadge.tsx      # Accessible status badge component
│   ├── GrowerProductCard.tsx      # Product grid item
│   ├── GrowerProductTable.tsx     # Sortable products table
│   ├── GrowerInventoryTable.tsx   # Real-time stock level table with adjust trigger
│   ├── GrowerStockStatus.tsx      # Stock health indicator
│   ├── GrowerStockAdjustDialog.tsx# Dialog form for stock adjustments (handles 409 Conflict)
│   ├── GrowerOrderTable.tsx       # Order fulfillment pipeline table
│   ├── GrowerOrderStatus.tsx      # Order status badge wrapper
│   ├── GrowerShipmentStatus.tsx   # Carrier shipment tracking badge
│   ├── GrowerEmptyState.tsx       # Empty state wrapper
│   ├── GrowerErrorState.tsx       # Error alert with retry action
│   └── GrowerSkeleton.tsx         # Skeleton loading placeholders
├── pages/
│   ├── GrowerDashboardPage.tsx    # Primary operational overview
│   ├── GrowerProfilePage.tsx      # Business identity profile page
│   ├── GrowerProductsPage.tsx     # Catalog management page
│   ├── GrowerInventoryPage.tsx    # Stock management page
│   ├── GrowerOrdersPage.tsx       # Order fulfillment page
│   ├── GrowerShipmentsPage.tsx    # Carrier tracking page
│   ├── GrowerReportsPage.tsx      # Analytics & reporting page
│   └── GrowerSettingsPage.tsx     # Operational settings page
└── index.ts                       # Public feature barrel exports
```

## Security & Authorization
Grower pages are protected under the `/grower` route hierarchy via `GrowerLayout`. Requests send bearer tokens automatically via `apiClient`, and server-authoritative role checks (`ROLE_ADMIN`, `ROLE_CUSTOMER`) enforce resource permissions.

## TanStack Query Key Hierarchy
- `['grower', 'dashboard']`
- `['grower', 'profile']`
- `['grower', 'products', filters]`
- `['grower', 'inventory', sku]`
- `['grower', 'orders', filters]`
- `['grower', 'shipments', filters]`
- `['grower', 'reports', period]`
- `['grower', 'settings']`
