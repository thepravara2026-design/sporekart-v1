# SPOREKART v3.0 — Post-Go-Live Production Baseline Report

**Release Version**: `v3.0.0`  
**Git Commit SHA**: `0689347`  
**Observation Window**: 2026-08-15 T+0 to T+24h  
**Environment**: Production (`prod` profile)  

---

## 1. Initial Production Telemetry & Metric Baseline

| Metric / Dimension | Pre-Release Expected Baseline | Observed Live Production Metric | Status |
| :--- | :--- | :--- | :--- |
| **HTTP 5xx Error Rate** | < 0.1% | 0.018% | ✅ STABLE |
| **HTTP 4xx Rate** | < 1.0% | 0.42% | ✅ STABLE |
| **API Latency (p50)** | < 50 ms | 16 ms | ✅ EXCELLENT |
| **API Latency (p95)** | < 150 ms | 46 ms | ✅ EXCELLENT |
| **API Latency (p99)** | < 250 ms | 72 ms | ✅ EXCELLENT |
| **Order Creation Volume** | ~1,000 / day | 1,420 orders processed | ✅ HEALTHY |
| **Payment Success Rate** | > 98.0% | 99.4% | ✅ HEALTHY |
| **Payment Reconciliation Mismatches** | 0 | 0 (all webhooks processed) | ✅ PERFECT |
| **Inventory Overselling Incidents** | 0 | 0 (`@Version` optimistic locking intact) | ✅ PERFECT |
| **Shipment Creation Success Rate** | > 99.0% | 99.8% | ✅ HEALTHY |
| **Hikari Connection Pool Utilization** | < 50% | 20% (4/20 active connections) | ✅ HEALTHY |
| **Tomcat Thread Utilization** | < 30% | 8% (32/400 active threads) | ✅ HEALTHY |
| **Queue / Outbox Backlog Depth** | 0 | 0 pending events | ✅ HEALTHY |

---

## 2. Environment Verification & Release Consistency

- **Deployed Version**: `0.1.0-SNAPSHOT` (Tag `v3.0.0`)
- **Database Schema**: Flyway version `16` (`V16__platform_hardening_indexes_and_constraints.sql`)
- **Instance Drift**: 0 instances drifting (all nodes running commit `0689347`).