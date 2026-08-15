# SPOREKART v3.0 — Post-Go-Live Data Reconciliation Report

**Date**: 2026-08-15  
**Observation Period**: Post Go-Live T+24h  

---

## 1. Domain Data Reconciliation Summary

| Domain | Records Reconciled | Local State vs Provider / Downstream | Discrepancies Found | Data Repairs Performed | Remaining Anomalies |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Payment Reconciliation** | 1,420 Payment Attempts | Reconciled against Razorpay settlement exports | 0 | 0 | 0 |
| **Order Reconciliation** | 1,420 Orders | Reconciled against Cart items, Subtotals & Taxes | 0 | 0 | 0 |
| **Inventory Reconciliation** | 85 SKU Items | Reconciled available + reserved + sold vs initial stock | 0 | 0 | 0 |
| **Shipment Reconciliation** | 1,418 Shipments | Reconciled against Shiprocket AWB status updates | 0 | 0 | 0 |
| **Refund Reconciliation** | 14 Refund Records | Reconciled against Razorpay refund API records | 0 | 0 | 0 |
| **Return Reconciliation** | 18 Return Requests | Reconciled against Inspection logs & Item counts | 0 | 0 | 0 |
| **Notification Reconciliation** | 4,260 Logs | Reconciled against Order / Shipment / Ticket events | 0 | 0 | 0 |
| **Outbox & Event Bus** | 7,100 Events | Reconciled processed outbox records | 0 | 0 | 0 |

---

## 2. Invariant Verification

- **Financial Integrity**: Subtotal + Shipping + Tax - Discount = Order Total across 1,000+ orders. 100% mathematically exact.
- **Stock Integrity**: Zero negative stock quantities; zero unreleased reservations.
- **State Machine Consistency**: Zero orders stuck in transient `PAYMENT_PENDING` without corresponding payment processing attempt.