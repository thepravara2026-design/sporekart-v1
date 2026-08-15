# SPOREKART v3.0 — Production Capacity Baseline Report

**Date**: 2026-08-15

---

## 1. Observed Resource Utilization & Operating Limits

| Component | Tested Capacity Limit | Observed Live Utilization | Headroom Remaining | Bottleneck Threshold |
| :--- | :--- | :--- | :--- | :--- |
| **PostgreSQL Pool** | 20 Connections (HikariCP) | 4 Connections (20% avg) | 80% Headroom | Pool > 18 for 3 mins |
| **Tomcat Worker Threads**| 400 Threads | 32 Threads (8% avg) | 92% Headroom | Threads > 320 for 5 mins |
| **CPU Utilization** | 8 vCPU Cluster | 1.2 vCPU (15% avg) | 85% Headroom | CPU > 75% |
| **Memory Headroom** | 8 GB JVM Heap | 2.1 GB Heap (26% avg) | 74% Headroom | Heap > 85% |
| **Database IOPS** | 3,000 IOPS | 180 IOPS (6% avg) | 94% Headroom | Disk Queue > 5 |

---

## 2. Capacity Growth Forecast
Based on post-launch order throughput (1,420 orders/24h), the current single-instance deployment configuration comfortably supports up to **10,000 orders/day** before requiring horizontal instance scaling or Redis caching.