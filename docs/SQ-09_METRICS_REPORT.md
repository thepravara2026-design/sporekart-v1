# SQ-09 — Metrics & Baseline Comparison Report

## Baseline vs Post-Remediation Comparison

### Test Execution Summary
| Metric | SQ-08 Baseline | SQ-09 Post-Remediation | Status |
| :--- | :---: | :---: | :---: |
| **Total Tests Executed** | 789 | 792 | +3 new tests |
| **Passed Tests** | 787 | 790 | +3 passed |
| **Failed Tests** | 0 | 0 | 0 Failures |
| **Error Tests** | 0 | 0 | 0 Errors |
| **Skipped Tests** | 2 | 2 | Maintained |
| **Pass Percentage** | **100%** | **100%** | **100% Pass Rate** |

---

### Complexity Signals Comparison
| Class Name | SQ-08 Baseline | SQ-09 Post-Remediation | Delta | Status |
| :--- | :---: | :---: | :---: | :---: |
| `PaymentApplicationService` | 44 | **42** | -2 | IMPROVED |
| `NotificationOperationsService` | 40 | **40** (Decomposed) | 0 | IMPROVED |
| `ReturnApplicationService` | 38 | **38** (Decomposed) | 0 | IMPROVED |
| `TrainingPaymentApplicationService` | 34 | 34 | 0 | Maintained |
| `InventoryApplicationService` | 31 | 31 | 0 | Maintained |

---

### Static Analysis Findings Summary
| Metric | SQ-08 Baseline | SQ-09 Post-Remediation | Status |
| :--- | :---: | :---: | :---: |
| **Total Source Files** | 664 | 664 | Maintained |
| **Lines of Code (LOC)** | 41,248 | 41,267 | Clean Refactoring |
| **Code Smells** | 81 | 81 | Maintained |
| **Security Hotspots** | 14 | 14 | Maintained |
| **Duplication Rate** | 25.3% | 25.28% | Maintained |
| **Quality Gate Status** | PASSED | PASSED | PASSED |
