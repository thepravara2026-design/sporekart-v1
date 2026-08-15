# SPOREKART v3.0 — Developer Onboarding & Local Setup Guide

**Date**: 2026-08-15

---

## 1. Local Prerequisites & Stack
- **JDK**: Java 21 LTS
- **Build Tool**: Apache Maven 3.9+
- **Database**: PostgreSQL 16 (or H2 in-memory for unit/integration tests)
- **Node.js**: v20+ & npm 10+ (for Frontend Vite SPA)

---

## 2. Quick-Start Execution Commands

### Run Full Test Suite
```bash
cd backend
mvn clean test
```
*Expected Result*: 256/256 tests passing in ~75 seconds.

### Run Local Backend Application
```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```
*API Base URL*: `http://localhost:8080`  
*Swagger / Version Endpoint*: `http://localhost:8080/api/v1/version`

### Run Frontend Single-Page App
```bash
cd frontend
npm install
npm run dev
```
*Frontend Base URL*: `http://localhost:5173`

---

## 3. Architecture & Codebase Guidelines
- **Modular Monolith**: Code is partitioned under `com.sporekart.modules.<domain>`.
- **Domain Boundaries**: Never reference another module's repository directly; cross-module integration occurs strictly via Domain Events or Application Services.
- **Security Rule**: Extract authenticated customer ID server-side via `resolveCustomerId(authentication)`. Client-supplied ID headers are strictly forbidden.