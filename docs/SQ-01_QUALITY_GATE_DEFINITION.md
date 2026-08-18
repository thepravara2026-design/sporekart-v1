# SQ-01 Deliverable F — SonarQube Quality Gate Definition

## 1. Executive Summary
This document establishes the official production Quality Gate criteria for the SPOREKART v3.0 Java/Spring Boot backend (`sporekart-backend`).

During SQ-01 (Baseline Phase), the current repository state is evaluated against these thresholds to establish an unvarnished quality baseline. Subsquent remediation sprints (SQ-02 through SQ-09) will systematically resolve findings to achieve full Quality Gate compliance (`PASS`).

---

## 2. Production Quality Gate Conditions

| Metric | Target / Condition | Severity Level | Production Gate Enforcement |
|---|---|---|---|
| **Blocker Bugs** | `= 0` | BLOCKER | **BLOCKING** |
| **Critical Bugs** | `= 0` | CRITICAL | **BLOCKING** |
| **Critical Vulnerabilities** | `= 0` | CRITICAL | **BLOCKING** |
| **High Vulnerabilities** | `= 0` | HIGH | **BLOCKING** |
| **Security Hotspots Reviewed** | `100%` | HIGH | **BLOCKING** |
| **New Code Line Coverage** | `>= 90.0%` | HIGH | **BLOCKING** |
| **Overall Backend Line Coverage** | `>= 85.0%` | HIGH | **BLOCKING** |
| **Overall Backend Branch Coverage** | `>= 80.0%` | HIGH | **BLOCKING** |
| **Duplicated Lines Percentage** | `< 3.0%` | MEDIUM | **BLOCKING** |
| **Cognitive Complexity (Per Method)** | `<= 15` | MEDIUM | **WARNING** |
| **Cyclomatic Complexity (Per Class)** | `<= 30` | MEDIUM | **WARNING** |
| **Unit & Integration Test Status** | `100% PASS (0 Failures, 0 Errors)` | BLOCKER | **BLOCKING** |

---

## 3. Evaluation Rules & Handling

### 3.1 New Code vs. Overall Code Policy
- **New Code**: Any commits or PRs submitted after SQ-01 baseline will be evaluated against strict New Code gates:
  - 0 New Critical / Blocker Bugs
  - 0 New Vulnerabilities
  - Coverage on New Code `>= 90%`
  - Duplication on New Code `< 1.5%`
- **Overall Code**: Evaluated against full repository quality baseline.

### 3.2 Non-Suppression Policy
- **No False Metric Inflation**: Code exclusions or `@SuppressWarnings("sonar:...")` annotations are strictly forbidden unless accompanied by a written security/architecture justification signed off in code review.
- **No Test Deletion**: Failing tests must be remediated or fixed in code, never deleted to improve pass rates or coverage.

---

## 4. Current SQ-01 Baseline Status

- **Quality Gate Evaluation**: `FAIL` (Expected for baseline sprint due to pre-existing test compilation issues, missing JaCoCo coverage baseline, and pending code smells).
- **Target Gate Compliance**: Scheduled for SQ-09 completion.
