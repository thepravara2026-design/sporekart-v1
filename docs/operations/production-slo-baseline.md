# SPOREKART v3.0 — Production SLO & Error Budget Baseline

**Date**: 2026-08-15

---

## 1. Service Level Objectives (SLOs) & Service Level Indicators (SLIs)

| Service Area | Service Level Indicator (SLI) | Target SLO | Observed Live SLI | Error Budget Remaining |
| :--- | :--- | :--- | :--- | :--- |
| **System Availability** | Successful non-5xx requests / Total requests | **99.9%** | 99.982% | 82% budget remaining |
| **API Latency (p95)** | Requests completed in < 150ms / Total requests | **95.0%** | 98.4% | 68% budget remaining |
| **Checkout Success** | Orders created / Checkout sessions started | **98.0%** | 99.1% | 55% budget remaining |
| **Payment Verification** | Verified payments / Total payment attempts | **99.0%** | 99.6% | 60% budget remaining |
| **Job Execution Health** | Successful background jobs / Total executed jobs | **99.5%** | 100.0% | 100% budget remaining |