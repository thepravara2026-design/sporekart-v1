# SPOREKART v3.0 — PAC-04 Source of Truth & Seller / Grower Architecture

**Document ID:** `PAC-04-SOURCE-OF-TRUTH`  
**Sprint:** `PAC-04 — Seller & Grower End-to-End Acceptance`  
**Author:** Antigravity FAANG Master Execution Agent  
**Date:** 2026-08-19  
**Status:** Certified Authoritative Baseline  

---

## 1. Executive Summary

This document establishes the single source of truth for SPOREKART v3.0 Seller and Grower workspace architectures, multi-tenant isolation rules, inventory management contracts, order visibility boundaries, and role authorization policies certified under **PAC-04 — Seller & Grower End-to-End Acceptance**.

---

## 2. Seller & Grower Architecture Overview

```
                        Security & Role Boundary Layer
   ┌───────────────────────────────────┬───────────────────────────────────┐
   │        ROLE_SELLER (Seller)       │        ROLE_GROWER (Grower)       │
   └─────────────────┬─────────────────┴─────────────────┬─────────────────┘
                     │                                   │
      Seller Workspace Controllers             Grower Workspace Controllers
   ┌───────────────────────────────────┐     ┌───────────────────────────────────┐
   │ - SellerProductController         │     │ - GrowerController                │
   │ - SellerInventoryController       │     │ - GrowerShipmentController        │
   │ - SellerOrderManagementController │     │ - GrowerReportsController         │
   └─────────────────┬─────────────────┘     └─────────────────┬─────────────────┘
                     │                                   │
                     └─────────────────┬─────────────────┘
                                       ▼
                       Application Domain & Security Filter
       - Server-side identity resolution (`sellerId` / `growerId` from JWT)
       - `AccessDeniedException` on cross-tenant IDOR access
                     ┌─────────────────┴─────────────────┐
                     │ Database Persistence & Isolation │
                     │ - product_entities (seller/grower)│
                     │ - inventory_items (sku/grower)    │
                     │ - orders (seller_id/grower_id)    │
                     │ - grower_profiles                 │
                     └───────────────────────────────────┘
```

---

## 3. Authoritative Module & Entity Mapping

| Domain Context | Application Service / Controller | Primary Entities / Repositories | Core Security Rule |
|----------------|----------------------------------|---------------------------------|--------------------|
| **Seller Products** | `SellerProductController` | `ProductEntity`, `ProductRepository` | `seller_id` matching JWT principal |
| **Seller Inventory** | `SellerInventoryController` | `InventoryItem`, `InventoryRepository` | SKU / Seller ownership check |
| **Seller Orders** | `SellerOrderManagementController` | `OrderEntity`, `OrderRepository` | Filtered by Seller products in order |
| **Grower Profile** | `GrowerController`, `GrowerApplicationService` | `GrowerProfileEntity`, `GrowerProfileRepository` | `grower_id` matching `user_id` |
| **Grower Products** | `GrowerController`, `GrowerApplicationService` | `ProductEntity`, `ProductRepository` | `grower_id` matching JWT principal |
| **Grower Inventory** | `GrowerController`, `GrowerApplicationService` | `InventoryItem`, `InventoryRepository` | SKU / `grower_id` isolation |
| **Grower Orders** | `GrowerController`, `GrowerApplicationService` | `OrderEntity`, `OrderRepository` | Filtered by Grower products in order |

---

## 4. Multi-Tenant Isolation Principles

1. **Server-Side Identity Resolution:** All workspace operations resolve `sellerId` or `growerId` directly from the authenticated `UserPrincipal` in `SecurityContextHolder`. Request payload spoofing is strictly overridden or rejected.
2. **Cross-Tenant Access Denial:** Attempting to query, update, or transition a product, inventory item, or order belonging to another Seller or Grower throws `AccessDeniedException` (`HTTP 403 FORBIDDEN`).
3. **Customer Privacy Protection:** Seller and Grower order views expose only fulfillment-relevant fields (shipping address snapshot, ordered line items, status). Customer PII, JWT tokens, and credentials are strictly excluded.

---

## 5. Governance Alignment Verdict

**VERDICT: CERTIFIED PASS** — The Seller and Grower architecture enforces server-side tenant isolation, authoritative domain security, and transactional consistency required for PAC-04.
