# Local Development Setup Guide — Sporekart v3.0

## System Requirements

- **Operating System**: Linux / macOS / Windows 11
- **Java**: Java 21 LTS (Temurin recommended)
- **Maven**: Apache Maven 3.9+
- **Node.js**: Node.js v20+ / npm v10+
- **Docker**: Docker Desktop / Docker Engine 24+

## Step-by-Step Local Setup

### 1. Clone & Branch Initialization
```bash
git clone https://github.com/thepravara2026-design/sporekart-v1.git sporekart
cd sporekart
git checkout develop
```

### 2. Environment Configuration
Copy the example environment template:
```bash
cp .env.example .env
```

### 3. Backend Execution (Java 21 / Spring Boot)

#### Development Profile (H2 In-Memory DB + Web Console)
```bash
cd backend
mvn clean test
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```
- Backend REST API: `http://localhost:8080`
- H2 Web Console: `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:sporekart_dev`, User: `sa`, Password: empty)

#### QAT / Testing Profile
```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=qat
```

### 4. Frontend Execution
From the root directory:
```bash
cd frontend
npm install
npm run lint
npm run test
npm run dev
```
The frontend starts on `http://localhost:5173`.

### 5. Verification
- Backend Health API: `http://localhost:8080/api/v1/health`
- Backend Version API: `http://localhost:8080/api/v1/version`
- Frontend Health UI: `http://localhost:5173/health`
