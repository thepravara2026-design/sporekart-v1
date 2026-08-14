# Contributing & Git Workflow Guidelines — Sporekart v3.0

## Git Branch Model

- `main`: Production-ready code only.
- `develop`: Integration branch for sprint development.
- `feature/*`: Feature development (e.g. `feature/sprint-1-auth`).
- `bugfix/*`: Bug fixes.
- `chore/*`: Maintenance, infrastructure, dependency updates.
- `release/*`: Release candidate testing.
- `hotfix/*`: Emergency production hotfixes.

## Branch Workflow Procedure

1. Fetch latest changes and checkout `develop`:
   ```bash
   git checkout develop
   git pull --ff-only origin develop
   ```
2. Create topic branch:
   ```bash
   git checkout -b feature/sprint-1-auth
   ```
3. Commit logically separated changes:
   ```bash
   git status
   git diff
   git add <intended-files>
   git diff --staged
   git commit -m "feat(auth): implement JWT token provider baseline"
   ```
4. Push and open Pull Request into `develop`.
5. Require CI checks to pass before merging into `develop`.
