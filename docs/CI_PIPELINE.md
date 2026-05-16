<p align="center">
  <img src="https://img.shields.io/badge/GitHub%20Actions-2088FF?style=for-the-badge&logo=githubactions&logoColor=white" />
  <img src="https://img.shields.io/badge/Java-21%20Temurin-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" />
  <img src="https://img.shields.io/badge/Maven-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white" />
  <img src="https://img.shields.io/badge/Ubuntu-Latest-E95420?style=for-the-badge&logo=ubuntu&logoColor=white" />
</p>

# ⚡ FinVault — CI/CD Pipeline Documentation

> **Workflow file:** `.github/workflows/backend-ci.yml`  
> **Platform:** GitHub Actions | **Runner:** `ubuntu-latest` | **JDK:** Eclipse Temurin 21

---

## 📑 Table of Contents

| # | Section |
|:-:|---------|
| 1 | [CI/CD Theory](#-cicd-theory) |
| 2 | [Why CI Matters for FinVault](#-why-ci-matters-for-finvault) |
| 3 | [GitHub Actions Overview](#-github-actions-overview) |
| 4 | [Trigger Events](#-trigger-events) |
| 5 | [Pipeline Stages — Visual](#-pipeline-stages--visual) |
| 6 | [Step-by-Step Explanation](#-step-by-step-explanation) |
| 7 | [The YAML Workflow File](#-the-yaml-workflow-file) |
| 8 | [Maven Build Lifecycle](#-maven-build-lifecycle) |
| 9 | [Dependency Caching](#-dependency-caching) |
| 10 | [Branch Protection](#-branch-protection) |
| 11 | [Troubleshooting](#-troubleshooting) |

---

## 📖 CI/CD Theory

### CI — Continuous Integration

**Continuous Integration** automatically **builds and tests code every time a developer pushes**. Problems are caught immediately, not at release time.

```
  WITHOUT CI                              WITH CI (FinVault)
  ──────────                              ──────────────────
  3 developers push code                  3 developers push code
  Week later: "Let's build..."            Immediately: GitHub Actions triggers
  💥 Build fails!                         ✅ Pass → merge allowed
  "Whose commit broke it??"              ❌ Fail → PR blocked, fix is 1 commit away
```

### CD — Continuous Delivery / Deployment

| Term | Definition | FinVault Status |
|------|-----------|:---------------:|
| **Continuous Delivery** | Code is always deployable; human triggers deploy | 🔜 Planned |
| **Continuous Deployment** | Every passing build auto-deploys to production | 🔜 Future |

---

## 🎯 Why CI Matters for FinVault

| Problem (Without CI) | Solution (With CI) |
|----------------------|-------------------|
| "Works on my machine!" | CI builds on a fresh Ubuntu VM — consistent environment |
| Broken code merges silently | PR is **blocked** until build passes |
| Dependencies break after updates | CI re-downloads and validates all deps |
| No one runs the build locally | Automated — runs on every push |
| Build issues discovered late | **Fail fast** — discovered within minutes |

### 🎓 Interview Sound-Bite

> "CI is about **confidence**. Every green build proves the code compiles, packages, and is deployable. In FinVault, a broken build blocks the PR — forcing the fix before it reaches `main`."

---

## 🤖 GitHub Actions Overview

| Concept | Definition | FinVault Example |
|---------|-----------|------------------|
| **Workflow** | YAML file defining automation | `.github/workflows/backend-ci.yml` |
| **Event** | What starts the workflow | `push` to `main`, `pull_request` to `main` |
| **Job** | A set of steps on one runner | `build` |
| **Step** | A single task within a job | "Setup Java 21", "Run Maven" |
| **Runner** | VM that executes the job | `ubuntu-latest` |
| **Action** | Reusable step from marketplace | `actions/checkout@v4`, `actions/setup-java@v4` |

### Hierarchy

```
📄 Workflow (.yml)
  └── 🔔 Trigger (push, pull_request)
       └── 🏗️ Job: "build"
            ├── 🖥️ Runner: ubuntu-latest
            ├── Step 1: Checkout code
            ├── Step 2: Setup Java 21
            ├── Step 3: chmod +x mvnw
            └── Step 4: mvnw package -DskipTests
```

---

## 🔔 Trigger Events

```yaml
on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]
```

| Event | When It Fires | Example |
|:-----:|--------------|---------|
| `push` to `main` | Direct commit or merged PR | Merging `feature/login` into `main` |
| `pull_request` to `main` | PR opened, synchronized, or reopened | Opening a PR from `feature/transactions` |

### What is "synchronized"?

> Pushing new commits to an open PR fires `pull_request` with activity `synchronized`. CI **automatically re-runs** on every push to the PR branch — no manual trigger needed.

---

## 🔄 Pipeline Stages — Visual

```
╔═══════════════════════════════════════════════════════════════════════╗
║                        FinVault CI Pipeline                          ║
╠═══════════════════════════════════════════════════════════════════════╣
║                                                                       ║
║   🔔 GitHub Event (push to main / PR to main)                        ║
║        │                                                              ║
║        ▼                                                              ║
║   ┌───────────────────────────────────────────────────────────────┐   ║
║   │  🏗️ Job: build                                                │   ║
║   │  🖥️ Runner: ubuntu-latest (fresh VM every run)                │   ║
║   │                                                               │   ║
║   │  ┌─────────────────────────────────────────────────────────┐  │   ║
║   │  │ Step 1 — 📥 Checkout Repository                         │  │   ║
║   │  │ actions/checkout@v4                                     │  │   ║
║   │  └─────────────────────────┬───────────────────────────────┘  │   ║
║   │                            ▼                                  │   ║
║   │  ┌─────────────────────────────────────────────────────────┐  │   ║
║   │  │ Step 2 — ☕ Setup Java 21 (Eclipse Temurin)              │  │   ║
║   │  │ actions/setup-java@v4 + Maven dependency cache          │  │   ║
║   │  └─────────────────────────┬───────────────────────────────┘  │   ║
║   │                            ▼                                  │   ║
║   │  ┌─────────────────────────────────────────────────────────┐  │   ║
║   │  │ Step 3 — 🔧 Grant Execute Permission                    │  │   ║
║   │  │ chmod +x mvnw                                           │  │   ║
║   │  └─────────────────────────┬───────────────────────────────┘  │   ║
║   │                            ▼                                  │   ║
║   │  ┌─────────────────────────────────────────────────────────┐  │   ║
║   │  │ Step 4 — 📦 Build with Maven                            │  │   ║
║   │  │ ./mvnw package -DskipTests --batch-mode                 │  │   ║
║   │  └─────────────────────────┬───────────────────────────────┘  │   ║
║   │                            ▼                                  │   ║
║   └───────────────────────────────────────────────────────────────┘   ║
║        │                                                              ║
║        ▼                                                              ║
║   ✅ Build passed → PR is mergeable                                   ║
║   ❌ Build failed → PR blocked                                        ║
║                                                                       ║
╚═══════════════════════════════════════════════════════════════════════╝
```

---

## 🔍 Step-by-Step Explanation

| Step | Action | What It Does | Why |
|:----:|--------|-------------|-----|
| 1 | `actions/checkout@v4` | Clones the repo into the runner's workspace | Runner starts empty — needs the code |
| 2 | `actions/setup-java@v4` | Installs Eclipse Temurin JDK 21 + configures Maven cache | Consistent Java version; speeds up builds |
| 3 | `chmod +x mvnw` | Sets execute permission on Maven Wrapper script | Linux requires explicit execute bit on scripts |
| 4 | `./mvnw package -DskipTests` | Compiles code → processes resources → packages into JAR | Validates compilation and packaging |

### 🎓 Why `-DskipTests`?

> Tests require a running MySQL database. The CI environment doesn't have one configured yet (will be added when the test suite is built). The current pipeline validates that **compilation and packaging succeed** — the minimum gate before merge.

---

## 📄 The YAML Workflow File

```yaml
# .github/workflows/backend-ci.yml
name: FinVault Backend CI

on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]

jobs:
  build:
    runs-on: ubuntu-latest

    defaults:
      run:
        working-directory: ./backend

    steps:
      # Step 1: Get the code
      - name: Checkout repository
        uses: actions/checkout@v4

      # Step 2: Install Java 21 + cache Maven dependencies
      - name: Setup Java 21 (Eclipse Temurin)
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
          cache: 'maven'

      # Step 3: Make Maven Wrapper executable (Linux)
      - name: Grant execute permission to mvnw
        run: chmod +x mvnw

      # Step 4: Compile + package (skip tests — no DB in CI yet)
      - name: Build with Maven
        run: ./mvnw package -DskipTests --batch-mode
```

---

## 📦 Maven Build Lifecycle

The `package` phase triggers all preceding phases:

```
validate → compile → test-compile → test → package
              ↑                                ↑
              |                                |
         FinVault runs              FinVault stops here
         from here                  (with -DskipTests)
```

| Phase | What Happens |
|-------|-------------|
| `validate` | Checks POM is valid |
| `compile` | Compiles `src/main/java` → `target/classes` |
| `test-compile` | Compiles `src/test/java` → `target/test-classes` |
| `test` | Runs unit tests (skipped) |
| `package` | Creates JAR: `target/backend-0.0.1-SNAPSHOT.jar` |

---

## 💾 Dependency Caching

```yaml
- uses: actions/setup-java@v4
  with:
    cache: 'maven'   # ← This line enables caching
```

| Run | Cache Status | Effect |
|:---:|:------------:|--------|
| First | MISS | Downloads all 60+ Maven deps (~2 min) |
| Subsequent | HIT | Restores from cache (~5 sec) |
| After POM change | MISS | Re-downloads changed deps |

### 🎓 Why Cache Maven? (Interview)

> Maven downloads 60+ dependencies on every build. Without caching, each CI run spends 1-2 minutes just downloading JARs. The `cache: 'maven'` directive stores `~/.m2/repository` between runs, reducing build time by 60-80%.

---

## 🔒 Branch Protection

When branch protection is configured on `main`:

| Rule | Effect |
|------|--------|
| Require status checks to pass | PR cannot merge until the `build` job passes |
| Require PR reviews | At least 1 approval before merge |
| No force pushes | History is never rewritten on `main` |

```
Developer pushes → PR created → CI runs → ✅ Pass + ✅ Review → Merge allowed
                                         → ❌ Fail → Merge blocked
```

---

## 🐛 Troubleshooting

| Symptom | Cause | Fix |
|---------|-------|-----|
| `Permission denied: ./mvnw` | Execute bit not set | `chmod +x mvnw` step fixes this |
| `package does not exist` | Missing dependency in POM | Add the dependency to `pom.xml` |
| `Could not resolve dependencies` | Maven Central unreachable (rare) | Re-run the pipeline |
| Build passes locally but fails in CI | Java version mismatch | Confirm `java-version: '21'` matches local JDK |
| `OutOfMemoryError` during compilation | Large project exceeding runner limits | Add `MAVEN_OPTS: -Xmx1024m` to env |

---

<p align="center">
  <b>⚡ FinVault CI/CD Pipeline Documentation</b><br>
  <sub>GitHub Actions | Java 21 Temurin | Maven Wrapper | Dependency Caching</sub><br>
  <sub>Part of the <a href="../README.md">FinVault Documentation Suite</a></sub>
</p>
