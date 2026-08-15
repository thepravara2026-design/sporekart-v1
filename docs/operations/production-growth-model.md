# SPOREKART v3.0 — Production Growth & Capacity Model

**Date**: 2026-08-15

---

## 1. Observed vs Projected Growth Model

| Dimension | Observed Baseline (T+24h) | 3-Month Projection | 6-Month Projection | Growth Factor |
| :--- | :--- | :--- | :--- | :--- |
| **Daily Orders** | 1,420 orders/day | 3,500 orders/day | 8,000 orders/day | 5.6x |
| **Registered Customers**| 850 users | 5,000 users | 15,000 users | 17.6x |
| **Catalog Products** | 120 SKUs | 500 SKUs | 1,500 SKUs | 12.5x |
| **Database Disk Growth** | ~150 MB / month | ~450 MB / month | ~1.2 GB / month | 8.0x |
| **API Throughput** | ~15 req/sec peak | ~45 req/sec peak | ~120 req/sec peak | 8.0x |

---

## 2. Component Capacity Headroom Matrix

| Component | Current Capacity Limit | Current Utilization | 6-Month Projected Load | Headroom Status | Scaling Trigger |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Database Pool (HikariCP)** | 20 Connections | 4 Connections (20%) | 12 Connections (60%) | ✅ HEALTHY HEADROOM | Active > 16 connections |
| **Tomcat Worker Threads** | 400 Threads | 32 Threads (8%) | 96 Threads (24%) | ✅ HEALTHY HEADROOM | Active > 320 threads |
| **Application CPU** | 8 vCPUs | 1.2 vCPUs (15%) | 3.6 vCPUs (45%) | ✅ HEALTHY HEADROOM | CPU > 70% for 5 mins |
| **Application Memory** | 8 GB JVM Heap | 2.1 GB (26%) | 4.2 GB (52.5%) | ✅ HEALTHY HEADROOM | Heap > 80% |
| **PostgreSQL IOPS** | 3,000 IOPS | 180 IOPS (6%) | 540 IOPS (18%) | ✅ HEALTHY HEADROOM | Disk Queue > 5 |

---

## 3. Scaling Mechanics
- **Horizontal Scale Trigger**: Spin up additional Spring Boot app instance behind load balancer if CPU > 70% or HikariCP wait queue > 10.
- **Cache Scaling Trigger**: Introduce Redis cluster if catalog search p95 latency exceeds 100ms.