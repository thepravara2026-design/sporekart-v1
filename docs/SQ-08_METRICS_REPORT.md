# SQ-08 — Metrics & Baseline Comparison Report

## Baseline vs Post-Remediation Comparison

### Test Execution Summary
| Metric | SQ-07 Baseline | SQ-08 Post-Remediation | Status |
| :--- | :---: | :---: | :---: |
| **Total Tests Executed** | 782 | 789 | +7 new tests |
| **Passed Tests** | 780 | 787 | +7 passed |
| **Failed Tests** | 0 | 0 | 0 Failures |
| **Error Tests** | 0 | 0 | 0 Errors |
| **Skipped Tests** | 2 | 2 | Maintained |
| **Pass Percentage** | **100%** | **100%** | **100% Pass Rate** |

---

### Coverage Metrics Summary
| Package / Domain | Baseline Line | Final Line | Baseline Branch | Final Branch | Status |
| :--- | :---: | :---: | :---: | :---: | :---: |
| `com.sporekart.modules.review.controller` | 15.69% | **35.29%** | 0.0% | 0.0% | +19.6% Line |
| `com.sporekart.modules.support.controller` | 16.87% | **21.69%** | 15.0% | 15.0% | +4.8% Line |
| `com.sporekart.modules.support.application` | 52.17% | **55.56%** | 35.71% | 35.71% | +3.4% Line |
| `com.sporekart.modules.inventory.controller` | 51.43% | **57.14%** | 50.0% | 50.0% | +5.7% Line |
| `com.sporekart.modules.inventory.application` | 62.08% | **63.33%** | 41.94% | 41.94% | +1.25% Line |
| `com.sporekart.modules.training.application.reporting` | 44.57% | 44.57% | 23.33% | **24.17%** | +0.84% Branch |

---

### Static Analysis Findings Summary
| Metric | SQ-07 Baseline | SQ-08 Post-Remediation | Status |
| :--- | :---: | :---: | :---: |
| **Total Source Files** | 664 | 664 | Maintained |
| **Lines of Code (LOC)** | 41,248 | 41,248 | Maintained |
| **Code Smells** | 81 | 81 | Maintained |
| **Security Hotspots** | 14 | 14 | Maintained |
| **Duplication Rate** | 25.3% | 25.3% | Maintained |
| **Quality Gate Status** | PASSED | PASSED | PASSED |
