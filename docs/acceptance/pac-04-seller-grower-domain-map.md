# SPOREKART v3.0 — PAC-04 Seller & Grower Domain Map

**Document ID:** `PAC-04-SELLER-GROWER-DOMAIN-MAP`  
**Sprint:** `PAC-04 — Seller & Grower End-to-End Acceptance`  
**Author:** Antigravity FAANG Master Execution Agent  
**Date:** 2026-08-19  
**Status:** Certified Domain Map  

---

## 1. Executive Summary

This document maps all Seller and Grower REST endpoints, frontend routes, application services, request DTOs, and authorization requirements certified under PAC-04.

---

## 2. Seller Domain Capabilities & Mapping

| Capability | Frontend Route / Component | REST Endpoint | HTTP | Authorization | Entity / Domain Action |
|------------|----------------------------|---------------|------|---------------|------------------------|
| **Seller Dashboard** | `/seller/dashboard` (`SellerDashboardPage`) | `GET /api/v1/seller/dashboard` | GET | `ROLE_SELLER` | Returns sales metrics, order counts, product status |
| **Seller Products** | `/seller/products` (`SellerProductManagementPage`) | `GET /api/v1/seller/products` | GET | `ROLE_SELLER` | Lists Seller-owned products (`findAllBySellerId`) |
| **Create Seller Product** | `SellerProductManagementPage` | `POST /api/v1/seller/products` | POST | `ROLE_SELLER` | Saves new `ProductEntity` associated with Seller |
| **Update Seller Product** | `SellerProductManagementPage` | `PUT /api/v1/seller/products/{id}` | PUT | `ROLE_SELLER` | Updates price, status, description for owned product |
| **Seller Inventory** | `/seller/inventory` (`SellerInventoryPage`) | `GET /api/v1/seller/inventory` | GET | `ROLE_SELLER` | Lists stock levels for Seller products |
| **Adjust Seller Stock** | `SellerInventoryPage` | `POST /api/v1/seller/inventory/adjust` | POST | `ROLE_SELLER` | Updates `onHandQuantity` in `InventoryItem` |
| **Seller Orders** | `/seller/orders` (`SellerOrderManagementPage`) | `GET /api/v1/seller/orders` | GET | `ROLE_SELLER` | Lists customer orders containing Seller products |
| **Seller Order Transition**| `SellerOrderManagementPage` | `POST /api/v1/seller/orders/{id}/status` | POST | `ROLE_SELLER` | Transitions order state (e.g. `PROCESSING` -> `READY_FOR_FULFILMENT`) |

---

## 3. Grower Domain Capabilities & Mapping

| Capability | Frontend Route / Component | REST Endpoint | HTTP | Authorization | Entity / Domain Action |
|------------|----------------------------|---------------|------|---------------|------------------------|
| **Grower Profile** | `/grower/profile` (`GrowerProfilePage`) | `GET /api/v1/grower/profile` | GET | `ROLE_GROWER` | Retrieves `GrowerProfileEntity` |
| **Grower Dashboard** | `/grower/dashboard` (`GrowerDashboardPage`) | `GET /api/v1/grower/dashboard` | GET | `ROLE_GROWER` | Aggregates lab metrics, stock, orders |
| **Grower Products** | `/grower/products` (`GrowerProductsPage`) | `GET /api/v1/grower/products` | GET | `ROLE_GROWER` | Lists Grower-owned spawn/culture products |
| **Create Grower Product** | `GrowerProductsPage` | `POST /api/v1/grower/products` | POST | `ROLE_GROWER` | Saves new `ProductEntity` (`grower_id`) |
| **Grower Inventory** | `/grower/inventory` (`GrowerInventoryPage`) | `GET /api/v1/grower/inventory` | GET | `ROLE_GROWER` | Queries `InventoryItem` by `grower_id` |
| **Adjust Stock** | `GrowerInventoryPage` | `POST /api/v1/grower/inventory/adjust` | POST | `ROLE_GROWER` | Updates stock quantity & logs adjustment |
| **Grower Orders** | `/grower/orders` (`GrowerOrdersPage`) | `GET /api/v1/grower/orders` | GET | `ROLE_GROWER` | Queries orders for Grower-produced items |
| **Grower Order Transition**| `GrowerOrdersPage` | `POST /api/v1/grower/orders/{id}/status` | POST | `ROLE_GROWER` | Updates fulfillment status |
| **Grower Shipments** | `/grower/shipments` (`GrowerShipmentsPage`) | `GET /api/v1/grower/shipments` | GET | `ROLE_GROWER` | Lists lab shipments & AWB references |

---

## 4. Domain Map Verdict

**VERDICT: PASS** — The domain map accurately reflects 100% of implemented Seller and Grower workspace controllers, services, and routes.
