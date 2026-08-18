# SQ-04 — Quality Metrics & Coverage Report

## Executive Summary
This document compares the test suite execution results and JaCoCo code coverage metrics between the **SQ-03 Baseline** and the **SQ-04 Orders & Payments Hardening Result**.

---

## 1. Test Suite Execution Metrics

| Metric | SQ-03 Baseline | SQ-04 Result | Delta |
| :--- | :--- | :--- | :--- |
| **Total Tests Executed** | 764 | 768 | **+4 tests** |
| **Passed Tests** | 762 | 766 | **+4 tests** |
| **Failures** | 0 | 0 | 0 |
| **Errors** | 0 | 0 | 0 |
| **Skipped** | 2 | 2 | 0 |
| **Pass Rate** | 100% | 100% | 0.00% |

---

## 2. JaCoCo Coverage Breakdown for Hardened Domains

| Domain / Package | SQ-04 Line Coverage | SQ-04 Branch Coverage |
| :--- | :--- | :--- |
| `com.sporekart.modules.order.domain` | **85.59%** (196/229) | **29.35%** (27/92) |
| `com.sporekart.modules.payment.application` | **85.66%** (209/244) | **54.55%** (72/132) |
| `com.sporekart.modules.returns.application` | **64.53%** (151/234) | **26.92%** (21/78) |
| `com.sporekart.modules.checkout.infrastructure.adapter` | **100.0%** (30/30) | **75.0%** (3/4) |
| `com.sporekart.modules.checkout.controller` | **88.89%** (8/9) | **50.0%** (3/6) |

---

## 3. Training Acceptance Gate Status
- **Training Module Status**: **ACCEPTED**
- **Test Pass Rate**: 100% (All Training module unit and integration tests passed cleanly).
