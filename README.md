# Sporekart v3.0

Sporekart v3.0 is an enterprise e-commerce platform built as a **Modular Monolith** using Java 21, Spring Boot, React (Vite + TypeScript), and PostgreSQL (Supabase).

## Architecture Overview

```
                    SPOREKART

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
                  POSTGRESQL
```

## Technology Stack

- **Backend**: Java 21, Spring Boot 3.4+, Maven, Spring Data JPA, Spring Security, Flyway, PostgreSQL, Actuator
- **Frontend**: Vite, React 18+, TypeScript, TanStack Query, Axios, Vitest, React Testing Library
- **Database**: Supabase PostgreSQL
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
├── .github/workflows/    # CI/CD GitHub Actions
├── .editorconfig
├── .gitignore
├── .env.example
├── docker-compose.yml
└── README.md
```

## Quick Start (Local Development)

### Prerequisites

- Java 21 LTS
- Apache Maven 3.9+
- Node.js 20+ / npm 10+
- Docker & Docker Compose

### Running locally

1. **Clone & Setup Environment**
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

4. **Verify Health APIs**
   - Backend Health: `http://localhost:8080/api/v1/health`
   - Backend Version: `http://localhost:8080/api/v1/version`
   - Frontend Health Page: `http://localhost:5173/health`

## Documentation

- [Architecture Overview](docs/architecture/architecture-overview.md)
- [Module Boundaries](docs/architecture/module-boundaries.md)
- [Local Setup Guide](docs/development/local-setup.md)
- [Contributing & Git Workflow](docs/development/contributing.md)
- [API Standards](docs/api/api-guidelines.md)
- [Security Baseline](docs/security/security-baseline.md)
