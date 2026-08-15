# SPOREKART v3.0 — Admin Operations Guide

**Date**: 2026-08-15
**Security Policy**: Access to all `/api/v1/admin/**` endpoints strictly requires `ROLE_ADMIN` authentication.

---

## 1. Core Operations Procedures

### 1. Catalog & Inventory Management
- **View Low Stock Items**: `GET /api/v1/admin/inventory/low-stock?threshold=10`
- **Adjust Stock Level**: `POST /api/v1/admin/inventory/skus/{sku}/adjust` with payload `{"quantityDelta": 50, "reason": "Restock Batch 402"}`.

### 2. Order & Fulfillment Operations
- **List Pending Orders**: `GET /api/v1/admin/orders?status=PAID`
- **Trigger Fulfillment Handoff**: `POST /api/v1/admin/shipments/create` with `orderId`. Interacts with Shiprocket to assign AWB.
- **Cancel Order (Admin Override)**: `POST /api/v1/admin/orders/{orderRef}/cancel` with reason. Restores reserved inventory automatically.

### 3. Returns & Refunds Operations
- **Review Return Request**: `GET /api/v1/admin/returns?status=REQUESTED`
- **Record Inspection Outcome**: `POST /api/v1/admin/returns/{returnRef}/inspect` with `outcome: PASSED`.
- **Trigger Refund**: `POST /api/v1/admin/returns/{returnRef}/refund`. Executes provider refund via `RefundApplicationService` idempotently.

### 4. Support & Replacement Operations
- **Assign Support Ticket**: `POST /api/v1/admin/support/tickets/{ticketNumber}/assign` with `agentId`.
- **Approve Replacement Request**: `POST /api/v1/admin/support/replacements/{replacementRef}/approve`.

### 5. Review Moderation
- **Moderate Product Reviews**: `POST /api/v1/admin/reviews/{reviewRef}/moderate` with `status: APPROVED` or `REJECTED`.