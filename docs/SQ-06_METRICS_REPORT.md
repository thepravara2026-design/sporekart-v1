# SQ-06 — Metrics & Baseline Comparison Report

## Baseline vs Post-Remediation Comparison

### Test Execution Summary
| Metric | Baseline | Post-Remediation |
| :--- | :---: | :---: |
| **Total Tests Executed** | 772 | 776 |
| **Passed Tests** | 770 | 774 |
| **Failed Tests** | 0 | 0 |
| **Error Tests** | 0 | 0 |
| **Skipped Tests** | 2 | 2 |
| **Pass Percentage** | **100%** | **100%** |

---

### Package Line Coverage Comparison
| Package / Module | Baseline Coverage | Post-Remediation Coverage | Status |
| :--- | :---: | :---: | :---: |
| `com.sporekart.application.outbox.application` | 78.17% | 78.62% | Improved (+0.45%) |
| `com.sporekart.modules.notification.domain` | 76.76% | 76.76% | Maintained |
| `com.sporekart.modules.notification.infrastructure` | 54.29% | 54.29% | Maintained |
| `com.sporekart.modules.notification.web` | 51.03% | 51.03% | Maintained |
| `com.sporekart.modules.training.domain` | 82.35% | 82.35% | Maintained |
| `com.sporekart.modules.payment.application` | 85.66% | 85.66% | Maintained |

---

### Static Analysis Code Quality Summary
| Metric | Count | Status |
| :--- | :---: | :---: |
| **Total Java Files** | 664 | Verified |
| **Total Lines of Code** | 41,247 | Maintained |
| **Code Smells** | 81 | Maintained |
| **Security Hotspots** | 14 | Maintained |
| **Duplicated Lines** | 25.3% | Maintained |
