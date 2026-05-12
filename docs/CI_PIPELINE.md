# FinVault — CI/CD Pipeline Documentation

> **Ticket:** SCRUM-12 | **Workflow file:** `.github/workflows/backend-ci.yml`  
> **Platform:** GitHub Actions | **Runner:** `ubuntu-latest` | **JDK:** Eclipse Temurin 21

---

## Overview

FinVault uses **GitHub Actions** as its CI/CD platform. The initial pipeline focuses on the **backend Continuous Integration** layer: it automatically verifies that the Spring Boot application compiles and packages into a deployable JAR on every push and pull request to `main`. This provides the team with immediate feedback if a code change breaks the build, before it can ever merge into the protected `main` branch.

---

## Trigger Events

| Event | Target Branch | When it fires |
|---|---|---|
| `push` | `main` | Every direct commit to `main` (e.g., after a PR merge) |
| `pull_request` | `main` | When a PR is opened, synchronized, or reopened against `main` |

---

## Pipeline Stages

```
GitHub Event (push / PR)
        │
        ▼
┌─────────────────────────────────────┐
│  Job: build                         │
│  Runner: ubuntu-latest              │
│                                     │
│  Step 1 ── Checkout repository      │
│  Step 2 ── Setup Java 21 (Temurin)  │  ← Maven cache restored here
│  Step 3 ── chmod +x mvnw            │  ← Linux permission fix
│  Step 4 ── mvnw package -DskipTests │  ← Compile + JAR, no tests
└─────────────────────────────────────┘
        │
        ▼
  ✅ Build passed → PR can be merged
  ❌ Build failed → PR is blocked
```

---

## Step-by-Step Explanation

### Step 1 — Checkout Repository
```yaml
uses: actions/checkout@v4
```
Clones the full repository into the GitHub Actions runner's ephemeral workspace so all source files are available.

---

### Step 2 — Set Up Java 21 (Eclipse Temurin)
```yaml
uses: actions/setup-java@v4
with:
  java-version: '21'
  distribution: 'temurin'
  cache: 'maven'
```
- **Temurin** is the Eclipse Foundation's free, open-source, production-grade OpenJDK build — the industry standard for Spring Boot CI pipelines.
- **`java-version: '21'`** matches our local development JDK, eliminating "works on my machine" discrepancies.
- **`cache: maven`** instructs the action to persist the `~/.m2/repository` folder between workflow runs. This avoids re-downloading all Spring Boot dependencies (~200MB+) on every run, significantly reducing build time.

---

### Step 3 — Grant Execute Permission to Maven Wrapper
```yaml
working-directory: ./backend
run: chmod +x mvnw
```
The `mvnw` shell script is committed without execute permissions on Windows (`git` on Windows does not set the executable bit by default). Linux runners require this permission to invoke the script. Skipping this step causes an immediate `Permission denied` failure on CI even if the build works locally.

---

### Step 4 — Build with Maven (Skip Tests)
```yaml
working-directory: ./backend
run: ./mvnw package -DskipTests --batch-mode --no-transfer-progress
```

| Flag | Purpose |
|---|---|
| `package` | Maven lifecycle phase: compile → process resources → compile tests → **package into JAR** |
| `-DskipTests` | Skips both test compilation **and** execution — per the team's SDET deferral policy |
| `--batch-mode` | Disables interactive prompts and ANSI colour codes — required for non-TTY CI environments |
| `--no-transfer-progress` | Suppresses per-file download progress bars that generate thousands of noisy log lines |
| `working-directory: ./backend` | Required because our monorepo has `pom.xml` inside `backend/`, not at the repo root |

---

## Branch Protection Integration

This workflow is designed to be registered as a **required status check** in GitHub's branch protection rules for `main`. Once configured:

- No PR can be merged unless the `build` job passes.
- Force-pushes to `main` are blocked.
- This enforces our quality gate without requiring manual reviewer intervention for every build.

---

## What Is NOT in This Pipeline (Deferred)

| Capability | Status | Future Ticket |
|---|---|---|
| Unit tests (JUnit / Mockito) | Deferred | SDET sprint |
| API tests (RestAssured) | Deferred | SDET sprint |
| UI tests (Selenium) | Deferred | SDET sprint |
| Docker image build & push | Planned | DevOps sprint |
| Deploy to staging environment | Planned | DevOps sprint |
| Angular frontend CI | Planned | Future SCRUM ticket |

---

*Last updated: Sprint 1 — SCRUM-12 (GitHub Actions Backend CI)*
