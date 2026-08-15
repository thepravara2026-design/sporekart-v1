# SPOREKART v3.0 — Developer Quick-Start Guide

**Target Audience**: Software Engineers & Contributors

---

## 1. Local Environment Setup

```bash
# 1. Clone & Switch to Working Branch
git clone <repo-url>
cd sporekart-v3.0

# 2. Run Backend Unit & Integration Tests (H2 In-Memory DB)
cd backend
mvn clean test

# 3. Launch Local Backend Server
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# 4. Launch Local Frontend Dev Server (Vite)
cd ../frontend
npm install
npm run dev
```