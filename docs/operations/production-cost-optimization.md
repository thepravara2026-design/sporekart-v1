# SPOREKART v3.0 — Production Cost Optimization & Unit Economics

**Date**: 2026-08-15

---

## 1. Production Cost Baseline per Unit Metric

| Infrastructure Component | Estimated Monthly Cost | Unit Metric Basis | Calculated Unit Cost | Optimization Target |
| :--- | :--- | :--- | :--- | :--- |
| **Backend Compute (App Instance)** | $60 / month | 42,600 orders / month | **$0.0014 / order** | Auto-scale idle instances |
| **PostgreSQL Database** | $120 / month | 42,600 orders / month | **$0.0028 / order** | Optimize connection pool & IOPS |
| **Log & Artifact Storage** | $30 / month | 42,600 orders / month | **$0.0007 / order** | 30-day log truncation policy |
| **Payment & Shipping Gateway Fees** | Pass-through % | Per transaction | ~1.8% per transaction | Reconcile provider tier discounts |
| **TOTAL DIRECT INFRASTRUCTURE COST**| **$210 / month** | **42,600 orders / month** | **$0.0049 / order** | Maintain infrastructure < 1% GMV |

---

## 2. Cost Governance Principles
- **No Reliability Tradeoffs**: Never downgrade database durability or security controls to save negligible infrastructure costs.
- **Log Noise Suppression**: Suppress DEBUG level logging in production; emit WARN/ERROR with correlation IDs only.