# SPOREKART v3.0 — FRONTEND PRODUCTION BLOCKERS MATRIX

**Audit ID:** FD-AUDIT-01  
**Date:** 2026-08-19  
**Branch:** `audit/fd-01-to-fd-25-completion`

---

## Production Readiness Summary

- **Overall Frontend Status:** **NOT PRODUCTION READY**
- **P0 (Production Blocker):** 2 Issues
- **P1 (Major Production Issue):** 2 Issues
- **P2 (Significant Quality Issue):** 4 Issues
- **P3 (Minor Improvement):** 1 Issue

---

## Detailed Blocker & Issue Inventory

| Defect ID | Severity | Category | Description | Impacted Components / Routes | Root Cause | Required Remediation |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **DEF-FD-01** | **P0** | Security / Auth | **Missing Frontend Auth & Role Guards (FD-18)** | `/grower/*`, `/seller/*` | `ProtectedRoute` and `RequireRole` components were never created or wrapped around `/grower` and `/seller` routes in `App.tsx`. | Create `ProtectedRoute` wrapper component and enforce authentication and role authorization for Grower and Seller routes in `App.tsx`. |
| **DEF-FD-02** | **P0** | Routing / Architecture | **Unrouted Admin Consoles & Trainee Experience (FD-13, FD-16)** | `/admin/*`, `/training/*` | `AdminReturnListPage`, `AdminTrainingOperationsConsole`, `BatchManagementConsole`, `NotificationOperationsConsole`, `TrainingProgramManagement`, `TrainingReportingConsole`, and `TraineeTrainingConsole` exist in `src/features/` but are NOT imported or routed in `App.tsx`. | Define `/admin/*` and `/training/*` routes with lazy loading and proper layout shells in `App.tsx`. |
| **DEF-FD-03** | **P1** | Build / Quality | **ESLint Static Analysis Failure (55 Errors)** | `admin/api/batchApi.ts`, `admin/pages/*`, `trainee/pages/*`, `grower/api/growerApi.ts` | Code written in admin, trainee, and grower features contains unhandled `@typescript-eslint/no-explicit-any` and `@typescript-eslint/no-unused-vars`. | Fix explicit `any` types and remove unused variables across admin, trainee, and grower feature modules to achieve clean `npm run lint`. |
| **DEF-FD-04** | **P1** | Testing / Integration | **Missing Live Backend E2E & Visual QA Test Suites (FD-22, FD-23)** | E2E Testing Framework | FD-22 (Live Spring Boot integration verification) and FD-23 (Playwright visual regression suite) were skipped without dedicated test pipelines. | Set up Playwright visual regression testing and an automated E2E integration test suite connected to a running Spring Boot test instance. |
| **DEF-FD-05** | **P2** | SEO / Localization | **Missing Dynamic SEO Metadata & Localization (FD-19)** | Entire Frontend App | `React Helmet` / Meta tag management not implemented; Kannada localized content not integrated into marketing pages. | Add dynamic title/meta tag provider and Kannada language content strings for public marketing surfaces. |
| **DEF-FD-06** | **P2** | UX / Mobile | **Missing Mobile Bottom Navigation Bar (FD-20)** | Mobile Viewports (320px–768px) | Mobile navigation relies on top header drawer menu; dedicated mobile bottom navigation bar was not implemented. | Add responsive fixed mobile bottom navigation component for core customer touchpoints. |
| **DEF-FD-07** | **P2** | Accessibility | **Missing Automated Accessibility Test Suite & High-Contrast Mode (FD-21)** | Accessibility Infrastructure | `axe-core` automated test runner and explicit high-contrast mode switcher were not added to Vitest/CI pipeline. | Integrate `@axe-core/react` or Vitest axe matcher to automate WCAG 2.2 AA compliance verification. |
| **DEF-FD-08** | **P2** | Performance | **Missing Formal Lighthouse & Performance Hardening (FD-24)** | Production Bundle | Production build generates 430kB index chunk; no Lighthouse benchmark (>90 target) or asset compression audit recorded. | Implement vendor bundle chunk splitting in `vite.config.ts` to reduce `index.js` chunk size below 250kB and record Lighthouse scores. |
| **DEF-FD-09** | **P3** | Documentation | **React Router v7 Future Flag Deprecation Warnings** | Vitest Console Output | Console logs emit warnings regarding React Router v6 → v7 `v7_startTransition` and `v7_relativeSplatPath` future flags. | Add `future={{ v7_startTransition: true, v7_relativeSplatPath: true }}` to `<BrowserRouter>` in `App.tsx`. |
