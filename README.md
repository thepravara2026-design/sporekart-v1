# Sporekart v3.0

Sporekart v3.0 is an enterprise e-commerce platform built as a **Modular Monolith** using Java 21, Spring Boot 3.4+, React (Vite + TypeScript), and PostgreSQL (Supabase / H2).

## Architecture Overview

```
                    SPOREKART v3.0

                         |
                         v

              +---------------------+
              |      FRONTEND       |
              |    Vite + React     |
              |     TypeScript      |
              +----------+----------+
                         |
                       REST
                         |
                         v
              +---------------------+
              |    SPRING BOOT      |
              |    API /api/v1      |
              +----------+----------+
                         |
        +----------------+----------------+
        |                |                |
        v                v                v
      AUTH            CATALOG          INVENTORY
        |                |                |
        +----------------+----------------+
                         |
        +----------------+----------------+
        |                |                |
        v                v                v
       CART            ORDER           PAYMENT
                         |
                         v
                     SHIPMENT
                         |
                         v
                   NOTIFICATION
                         |
                         v
                  POSTGRESQL / H2
```

## Technology Stack

- **Backend**: Java 21, Spring Boot 3.4+, Maven, Spring Data JPA, Spring Security, Flyway, PostgreSQL / H2, Springdoc OpenAPI
- **Frontend**: Vite, React 18+, TypeScript, TanStack Query, Axios, Vitest, React Testing Library
- **Database**: PostgreSQL (Supabase) / H2 in-memory DB for DEV/QAT
- **Infrastructure**: Docker, Docker Compose, GitHub Actions

## Repository Layout

```
sporekart/
├── frontend/             # Vite + React + TS Application
├── backend/              # Spring Boot Java Application
├── database/             # Schema & Migration SQL documentation
├── infrastructure/       # Docker & Deployment Infrastructure
├── scripts/              # Helper & Validation Scripts
├── docs/                 # Architecture, API & Development Documentation
│   ├── catalog/          # Catalog Module Documentation
│   │   ├── architecture.md
│   │   ├── api.md
│   │   ├── database.md
│   │   ├── testing.md
│   │   └── technical-debt.md
│   ├── adr/              # Architectural Decision Records
│   ├── releases/         # Release Baselines & Evidence Reports
│   └── troubleshooting.md
├── .github/workflows/    # CI/CD GitHub Actions
├── .env.example
├── docker-compose.yml
└── README.md
```

## Quick Start (Local Development)

### Prerequisites

- Java 21 LTS
- Apache Maven 3.9+
- Node.js 20+ / npm 10+

### Running Locally

1. **Setup Environment**
   ```bash
   cp .env.example .env
   ```

2. **Start Backend**
   ```bash
   cd backend
   mvn spring-boot:run
   ```

3. **Start Frontend**
   ```bash
   cd frontend
   npm install
   npm run dev
   ```

4. **Run Test Suites**
   - Backend Tests: `cd backend && mvn clean test` (50 tests passing)
   - Frontend Tests: `cd frontend && npm run test` (20 tests passing)

5. **Verify API Endpoints & Docs**
   - Backend Health: `http://localhost:8080/api/v1/health`
   - OpenAPI Spec: `http://localhost:8080/v3/api-docs`
   - Swagger UI: `http://localhost:8080/swagger-ui/index.html`
   - Catalog Products: `http://localhost:8080/api/v1/catalog/products`

## Module Documentation

- [Catalog Architecture](docs/catalog/architecture.md)
- [Catalog REST API Specification](docs/catalog/api.md)
- [Catalog Database Model & Schema](docs/catalog/database.md)
- [Cart Module Specification & Handoff](docs/cart.md)
- [Catalog Testing Strategy & Suite](docs/testing/catalog-testing.md)
- [Troubleshooting & Diagnostics Guide](docs/troubleshooting.md)
- [Technical Debt Register](docs/catalog/technical-debt.md)
- [Catalog Release Baseline Evidence](docs/releases/catalog-release-baseline.md)
- [ADR 0001: Catalog Architecture](docs/adr/0001-catalog-modular-monolith-architecture.md)
- [ADR 0002: Cart Architecture & Handoff](docs/adr/0002-cart-domain-architecture-and-checkout-handoff.md)
