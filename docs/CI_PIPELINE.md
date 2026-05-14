<p align="center">
  <img src="https://img.shields.io/badge/GitHub%20Actions-2088FF?style=for-the-badge&logo=githubactions&logoColor=white" />
  <img src="https://img.shields.io/badge/Java-21%20Temurin-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" />
  <img src="https://img.shields.io/badge/Maven-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white" />
  <img src="https://img.shields.io/badge/Ubuntu-Latest-E95420?style=for-the-badge&logo=ubuntu&logoColor=white" />
</p>

# ⚡ FinVault — CI/CD Pipeline Documentation

> **Ticket:** SCRUM-12 | **Workflow file:** `.github/workflows/backend-ci.yml`  
> **Platform:** GitHub Actions | **Runner:** `ubuntu-latest` | **JDK:** Eclipse Temurin 21

---

## 📑 Table of Contents

| # | Section | Description |
|:-:|---------|-------------|
| 1 | [What is CI/CD? — Theory](#-what-is-cicd--theory) | Foundational DevOps concepts |
| 2 | [Why CI/CD Matters for FinVault](#-why-cicd-matters-for-finvault) | The business case for automation |
| 3 | [GitHub Actions — Overview](#-github-actions--overview) | Understanding the CI platform |
| 4 | [Trigger Events](#-trigger-events) | When the pipeline runs |
| 5 | [Pipeline Stages — Visual Flow](#-pipeline-stages--visual-flow) | Step-by-step visual |
| 6 | [Step-by-Step Explanation](#-step-by-step-explanation) | Deep dive into each pipeline step |
| 7 | [The YAML Workflow File](#-the-yaml-workflow-file) | Annotated workflow configuration |
| 8 | [Maven Build Lifecycle](#-maven-build-lifecycle) | Understanding Maven phases |
| 9 | [Branch Protection Integration](#-branch-protection-integration) | Quality gates and merge rules |
| 10 | [Caching — Performance Optimization](#-caching--performance-optimization) | How dependency caching works |
| 11 | [What's Deferred (Future Sprints)](#-whats-deferred-future-sprints) | Roadmap for pipeline evolution |
| 12 | [Troubleshooting Common CI Failures](#-troubleshooting-common-ci-failures) | Common errors and fixes |
| 13 | [Glossary](#-glossary) | Key CI/CD terms |

---

## 📖 What is CI/CD? — Theory

### CI — Continuous Integration

**Continuous Integration** is the practice of **automatically building and testing code every time a developer pushes changes**. Instead of waiting until "release day" to discover that code is broken, CI catches problems immediately.

```
  WITHOUT CI (Traditional)                    WITH CI (FinVault)
  ─────────────────────────                   ────────────────────
  Developer A pushes code ─┐                  Developer A pushes code ─┐
  Developer B pushes code ─┤                  Developer B pushes code ─┤
  Developer C pushes code ─┤                  Developer C pushes code ─┤
                           │                                           │
  Week later: "Let's                          Immediately:
  try to build..."         │                  GitHub Actions triggers ──┤
                           ▼                                           ▼
  💥 Build fails!                             ✅ Build passes → merge
  "Whose code broke it??"                     ❌ Build fails → PR blocked
  🔍 3 days of debugging                     🔧 Fix is 1 commit away
```

### CD — Continuous Delivery / Deployment

| Term | Definition | FinVault Status |
|------|-----------|:---------------:|
| **Continuous Delivery** | Code is always in a deployable state; human triggers deployment | 🔜 Planned |
| **Continuous Deployment** | Every passing build is automatically deployed to production | 🔜 Future |

### The CI/CD Pipeline Concept

```
  ┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐
  │   CODE   │ →  │  BUILD   │ →  │   TEST   │ →  │  DEPLOY  │ →  │ MONITOR  │
  │          │    │          │    │          │    │          │    │          │
  │ Developer│    │ Compile  │    │ Unit     │    │ Staging  │    │ Logs     │
  │ pushes   │    │ & package│    │ Integr.  │    │ Prod     │    │ Alerts   │
  │ to Git   │    │ (Maven)  │    │ E2E tests│    │          │    │ Metrics  │
  └──────────┘    └──────────┘    └──────────┘    └──────────┘    └──────────┘
                  ▲                                              
                  │ FinVault is HERE (Sprint 1)                  
                  │ Build + Package into JAR                     
```

---

## 🎯 Why CI/CD Matters for FinVault

| Problem (Without CI) | Solution (With CI) |
|----------------------|-------------------|
| "It works on my machine!" | CI builds on a fresh Ubuntu runner — same environment every time |
| Broken code merges silently | PR is **blocked** until the build passes |
| Dependencies break after updates | CI re-downloads and validates all dependencies |
| No one runs the build locally | Automated — runs on every push automatically |
| Build issues discovered late | **Fail fast** — discovered within minutes of pushing |

> 💡 **Key insight:** CI isn't just about catching bugs — it's about **confidence**. Every green build proves the code compiles, packages, and is deployable.

---

## 🤖 GitHub Actions — Overview

### What is GitHub Actions?

**GitHub Actions** is GitHub's built-in CI/CD platform. It runs automated **workflows** in response to **events** (pushes, PRs, schedules, etc.) on cloud-hosted **runners** (virtual machines).

### Core Concepts

| Concept | Definition | FinVault Example |
|---------|-----------|------------------|
| **Workflow** | A YAML file that defines automation | `.github/workflows/backend-ci.yml` |
| **Event (Trigger)** | What starts the workflow | `push` to `main`, `pull_request` to `main` |
| **Job** | A set of steps that run on the same runner | `build` |
| **Step** | A single task within a job | "Setup Java 21", "Run Maven" |
| **Runner** | The virtual machine that executes the job | `ubuntu-latest` |
| **Action** | A reusable step (from the marketplace) | `actions/checkout@v4`, `actions/setup-java@v4` |

### Visual Hierarchy

```
  📄 Workflow (.yml file)
      │
      ├── 🔔 Trigger Events (push, pull_request)
      │
      └── 🏗️ Job: "build"
              │
              ├── 🖥️ Runner: ubuntu-latest
              │
              ├── Step 1: Checkout code
              ├── Step 2: Setup Java 21
              ├── Step 3: chmod +x mvnw
              └── Step 4: mvnw package
```

---

## 🔔 Trigger Events

| Event | Target Branch | When It Fires | Example |
|:-----:|:------------:|--------------|---------|
| `push` | `main` | Every direct commit to `main` (typically after PR merge) | Merging a feature branch PR |
| `pull_request` | `main` | When a PR is opened, synchronized, or reopened against `main` | Creating a new PR from `feature/login` |

### What is "synchronized"?

> When you push new commits to an **already open PR**, GitHub fires a `pull_request` event with `activity: synchronized`. This means the CI re-runs automatically whenever you push fixes to your PR branch.

```
  feature/login branch                  main branch
       │                                     │
       │── Push commit #1 ─────────────┐     │
       │   PR opened → CI runs ✅      │     │
       │                                │     │
       │── Push commit #2 (fix) ────────┤     │
       │   PR synchronized → CI runs ✅ │     │
       │                                │     │
       │── Push commit #3 (more fix) ───┤     │
       │   PR synchronized → CI runs ✅ │     │
       │                                │     │
       └────────────────────────────────┴────►│  Merged!
                                              │
```

---

## 🔄 Pipeline Stages — Visual Flow

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
║   │  🖥️ Runner: ubuntu-latest (fresh VM every time)               │   ║
║   │                                                               │   ║
║   │  ┌─────────────────────────────────────────────────────────┐  │   ║
║   │  │ Step 1 — 📥 Checkout Repository                         │  │   ║
║   │  │ actions/checkout@v4                                     │  │   ║
║   │  │ Clones the full repo into the runner's workspace        │  │   ║
║   │  └─────────────────────────┬───────────────────────────────┘  │   ║
║   │                            ▼                                  │   ║
║   │  ┌─────────────────────────────────────────────────────────┐  │   ║
║   │  │ Step 2 — ☕ Setup Java 21 (Eclipse Temurin)              │  │   ║
║   │  │ actions/setup-java@v4                                   │  │   ║
║   │  │ Installs JDK 21 + restores Maven dependency cache       │  │   ║
║   │  └─────────────────────────┬───────────────────────────────┘  │   ║
║   │                            ▼                                  │   ║
║   │  ┌─────────────────────────────────────────────────────────┐  │   ║
║   │  │ Step 3 — 🔧 Grant Execute Permission to mvnw            │  │   ║
║   │  │ chmod +x mvnw                                           │  │   ║
║   │  │ Linux requires execute bit on shell scripts             │  │   ║
║   │  └─────────────────────────┬───────────────────────────────┘  │   ║
║   │                            ▼                                  │   ║
║   │  ┌─────────────────────────────────────────────────────────┐  │   ║
║   │  │ Step 4 — 📦 Build with Maven (Skip Tests)               │  │   ║
║   │  │ ./mvnw package -DskipTests --batch-mode                 │  │   ║
║   │  │ Compile → Process Resources → Package → JAR             │  │   ║
║   │  └─────────────────────────┬───────────────────────────────┘  │   ║
║   │                            ▼                                  │   ║
║   └───────────────────────────────────────────────────────────────┘   ║
║        │                                                              ║
║        ▼                                                              ║
║   ✅ Build passed → PR is mergeable                                   ║
║   ❌ Build failed → PR is BLOCKED                                     ║
║                                                                       ║
╚═══════════════════════════════════════════════════════════════════════╝
```

---

## 📋 Step-by-Step Explanation

### Step 1 — 📥 Checkout Repository

```yaml
- uses: actions/checkout@v4
```

| What It Does | Why It's Needed |
|-------------|----------------|
| Clones the **full repository** into the runner's ephemeral workspace | The runner starts as a bare VM with no code — it needs the source files to build |

> 💡 **Without this step:** Maven would have nothing to compile — the runner's workspace would be empty.

---

### Step 2 — ☕ Set Up Java 21 (Eclipse Temurin)

```yaml
- uses: actions/setup-java@v4
  with:
    java-version: '21'
    distribution: 'temurin'
    cache: 'maven'
```

| Setting | Value | Purpose |
|---------|:-----:|---------|
| `java-version` | `'21'` | Matches our local dev JDK — eliminates "works on my machine" issues |
| `distribution` | `'temurin'` | Eclipse Foundation's free, production-grade OpenJDK — industry standard |
| `cache` | `'maven'` | Caches `~/.m2/repository` between runs — avoids re-downloading 200MB+ of dependencies |

### What is Eclipse Temurin?

> **Temurin** (formerly AdoptOpenJDK) is the Eclipse Foundation's free, open-source, production-grade build of OpenJDK. It's the most widely used JDK distribution in CI/CD pipelines because:
> - Free and open source (no licensing issues)
> - Cross-platform (Linux, macOS, Windows)
> - Long-term support (LTS) for Java 21
> - Pre-packaged as a GitHub Action for easy CI integration

---

### Step 3 — 🔧 Grant Execute Permission to Maven Wrapper

```yaml
- working-directory: ./backend
  run: chmod +x mvnw
```

| Problem | Solution |
|---------|---------|
| The `mvnw` script is committed from Windows (which doesn't set Unix execute permissions) | `chmod +x` adds the execute bit so Linux can run it |
| Without this: `Permission denied` error on the CI runner | With this: `mvnw` runs normally |

### Why Does This Happen?

```
  Windows                                Linux (CI Runner)
  ──────                                 ─────
  Git doesn't track execute bits         Linux REQUIRES execute bits
  mvnw is saved as a regular file        chmod +x adds the permission

  $ git ls-files -s mvnw                 $ ls -la mvnw
  100644 blob abc123  mvnw               -rw-r--r-- mvnw  ← Can't execute!
                                         
                                         $ chmod +x mvnw
                                         $ ls -la mvnw
                                         -rwxr-xr-x mvnw  ← Now it works!
```

---

### Step 4 — 📦 Build with Maven (Skip Tests)

```yaml
- working-directory: ./backend
  run: ./mvnw package -DskipTests --batch-mode --no-transfer-progress
```

| Flag | Purpose |
|:----:|---------|
| `package` | Maven lifecycle phase: compile → process resources → compile tests → **package into JAR** |
| `-DskipTests` | Skips both test compilation **and** execution — per the team's SDET deferral policy |
| `--batch-mode` | Disables interactive prompts and ANSI colour codes — required for CI (non-interactive terminal) |
| `--no-transfer-progress` | Suppresses per-file download progress bars (they generate thousands of noisy log lines) |
| `working-directory: ./backend` | Required because `pom.xml` lives inside `backend/`, not at the repo root (monorepo layout) |

---

## 📄 The YAML Workflow File

### Annotated: `.github/workflows/backend-ci.yml`

```yaml
# ================================================================
# FinVault Backend CI Pipeline
# Triggers on push/PR to main — builds the Spring Boot JAR
# ================================================================

name: Backend CI                            # Display name in GitHub UI

on:                                          # ──── TRIGGER EVENTS ────
  push:
    branches: [ main ]                       # Runs on direct pushes to main
  pull_request:
    branches: [ main ]                       # Runs on PRs targeting main

jobs:                                        # ──── JOBS ────
  build:                                     # Job name (shown in PR checks)
    runs-on: ubuntu-latest                   # Fresh Ubuntu VM with 7GB RAM, 14GB SSD

    steps:                                   # ──── STEPS ────

      # Step 1: Clone the repository
      - name: Checkout repository
        uses: actions/checkout@v4

      # Step 2: Install Java 21 + restore Maven cache
      - name: Set up Java 21 (Temurin)
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
          cache: 'maven'                     # Cache ~/.m2/repository

      # Step 3: Fix Linux execute permission for Maven Wrapper
      - name: Grant execute permission to mvnw
        working-directory: ./backend
        run: chmod +x mvnw

      # Step 4: Compile + package the Spring Boot JAR
      - name: Build with Maven (skip tests)
        working-directory: ./backend
        run: ./mvnw package -DskipTests --batch-mode --no-transfer-progress
```

### YAML Syntax Quick Reference

| Syntax | Meaning | Example |
|--------|---------|---------|
| `key: value` | Key-value pair | `name: Backend CI` |
| `- item` | List item | `- name: Checkout` |
| `[ a, b ]` | Inline list | `branches: [ main ]` |
| Indentation | Hierarchy (2 spaces) | Steps are children of a job |

---

## 🏗️ Maven Build Lifecycle

### What is Maven?

**Maven** is a build automation tool for Java projects. It manages:
- **Dependencies** — downloads libraries (Spring Boot, Hibernate, etc.) from Maven Central
- **Build lifecycle** — compiles, tests, and packages code in a standard sequence
- **Project structure** — enforces a conventional directory layout (`src/main/java`, `src/test/java`)

### Maven Lifecycle Phases

When you run `mvn package`, Maven executes these phases **in order**:

```
  validate → compile → test → package → verify → install → deploy
                                  ▲
                                  │
                           FinVault stops here
                     (package = compile + create JAR)
```

| Phase | What It Does | Executed by FinVault CI? |
|:-----:|-------------|:------------------------:|
| `validate` | Validates project structure and `pom.xml` | ✅ Yes (auto) |
| `compile` | Compiles `.java` files into `.class` bytecode | ✅ Yes (auto) |
| `test` | Runs unit tests (JUnit) | ❌ Skipped (`-DskipTests`) |
| `package` | Packages compiled code into a JAR/WAR file | ✅ Yes (target phase) |
| `verify` | Runs integration tests | ❌ Not reached |
| `install` | Copies JAR to local `~/.m2/repository` | ❌ Not reached |
| `deploy` | Uploads JAR to remote repository (Nexus/Artifactory) | ❌ Not reached |

### What is the Maven Wrapper (`mvnw`)?

```
  WITH Maven installed globally:          WITH Maven Wrapper:
  ─────────────────────────────          ─────────────────────
  Developer must install Maven            No installation needed!
  "Which version? 3.8? 3.9? 4.0?"        ./mvnw spring-boot:run
  Works only if PATH is set correctly     Wrapper downloads correct version
  Different devs might use different      Everyone uses the SAME version
  versions → "works on my machine"        → Reproducible builds ✅
```

---

## 🛡️ Branch Protection Integration

### What is Branch Protection?

**Branch protection rules** prevent direct changes to important branches (like `main`). Combined with CI, they create an **automated quality gate**:

```
  Developer creates PR → CI runs automatically
                              │
                              ├── ✅ Build passes → "Merge" button is GREEN
                              │
                              └── ❌ Build fails → "Merge" button is BLOCKED 🔴
                                   Developer must fix and push again
```

### Recommended Branch Protection Settings for FinVault

| Setting | Value | Purpose |
|---------|:-----:|---------|
| Require status checks to pass | ✅ `build` job | PRs can't merge until CI says "green" |
| Require branches to be up to date | ✅ Enabled | PR must be rebased on latest `main` |
| Require pull request reviews | ✅ 1 reviewer | At least one team member must approve |
| Restrict force pushes | ✅ Blocked | Prevents rewriting `main` branch history |
| Restrict deletions | ✅ Blocked | Prevents accidental `main` branch deletion |

---

## 📦 Caching — Performance Optimization

### The Problem Without Caching

```
  Run 1: mvnw package                    Run 2 (no cache): mvnw package
  ─────────────────                       ──────────────────────────────
  Download spring-boot-starter (50MB)     Download spring-boot-starter (50MB) ← Again!
  Download hibernate-core (20MB)          Download hibernate-core (20MB)      ← Again!
  Download mysql-connector (5MB)          Download mysql-connector (5MB)      ← Again!
  ... 200+ dependencies (~200MB)          ... 200+ dependencies (~200MB)      ← Again!
  ⏱️ Total: 3-5 minutes                  ⏱️ Total: 3-5 minutes              ← Same time!
```

### The Solution: `cache: 'maven'`

```yaml
- uses: actions/setup-java@v4
  with:
    cache: 'maven'     # ← This one line saves minutes per build
```

```
  Run 1 (cold cache):                    Run 2 (warm cache):
  ────────────────────                    ─────────────────────
  Download all deps (~200MB)              Restore cache (~2 seconds)
  Build the project                       Build the project
  Save deps to cache                      ⏱️ Total: 30-60 seconds ← Much faster!
  ⏱️ Total: 3-5 minutes
```

### How It Works Internally

```
  End of Build → GitHub Action saves ~/.m2/repository as a cache artifact
                      │
                      ├── Key: hash of pom.xml (changes when deps change)
                      │
                      ▼
  Next Build → Checks if cache exists for this pom.xml hash
                      │
                      ├── ✅ Cache hit → Restore deps instantly (skip downloads)
                      └── ❌ Cache miss → Download all deps (new pom.xml detected)
```

---

## 🔮 What's Deferred (Future Sprints)

| Capability | Current Status | Future Sprint | Priority |
|:----------:|:--------------:|:------------:|:--------:|
| Unit tests (JUnit / Mockito) | ❌ Deferred | SDET sprint | 🔴 High |
| API tests (RestAssured) | ❌ Deferred | SDET sprint | 🔴 High |
| UI tests (Selenium) | ❌ Deferred | SDET sprint | 🟡 Medium |
| Docker image build & push | ❌ Planned | DevOps sprint | 🟡 Medium |
| Deploy to staging environment | ❌ Planned | DevOps sprint | 🟡 Medium |
| Angular frontend CI (`ng build`) | ❌ Planned | Future ticket | 🟢 Normal |
| Code quality gates (SonarQube) | ❌ Future | Quality sprint | 🟢 Normal |
| Security scanning (OWASP) | ❌ Future | Security sprint | 🟡 Medium |

---

## 🔧 Troubleshooting Common CI Failures

| # | Error | Cause | Fix |
|:-:|-------|-------|-----|
| 1 | `Permission denied: ./mvnw` | Missing execute bit on Linux | Ensure Step 3 (`chmod +x mvnw`) is present |
| 2 | `Could not find or load main class` | Wrong `working-directory` | Verify `working-directory: ./backend` on Maven step |
| 3 | `Compilation failure` | Code doesn't compile | Fix Java compilation errors locally, push again |
| 4 | `Cannot resolve dependencies` | Private/missing dependency in `pom.xml` | Check dependency coordinates in `pom.xml` |
| 5 | `No such file: pom.xml` | Maven ran in wrong directory | Add `working-directory: ./backend` to the step |
| 6 | `Java version mismatch` | CI uses different JDK than local | Ensure `java-version: '21'` matches `pom.xml` |
| 7 | Workflow never triggers | YAML indentation error | Validate YAML syntax; check `on:` trigger config |
| 8 | Cache not restoring | `pom.xml` changed (new hash) | Expected behavior — cache rebuilds on dep changes |

---

## 📚 Glossary

| Term | Definition |
|------|-----------|
| **CI** | Continuous Integration — automatically building and testing code on every push |
| **CD** | Continuous Delivery/Deployment — automatically deploying passing builds to staging or production |
| **Pipeline** | A sequence of automated stages (build, test, deploy) that code passes through |
| **Workflow** | A GitHub Actions YAML file that defines an automation pipeline |
| **Job** | A set of steps that run on the same runner (VM) within a workflow |
| **Step** | A single task within a job — either a shell command or a reusable Action |
| **Runner** | A GitHub-hosted virtual machine (Ubuntu, Windows, or macOS) that executes jobs |
| **Action** | A reusable workflow unit published on GitHub Marketplace (e.g., `actions/checkout@v4`) |
| **Trigger (Event)** | The condition that starts a workflow (push, pull_request, schedule, etc.) |
| **Branch Protection** | GitHub rules that prevent direct changes to protected branches without passing CI |
| **Quality Gate** | An automated check that code must pass before it can be merged or deployed |
| **Maven Wrapper** | A script (`mvnw`) that downloads and runs a specific Maven version — no global install needed |
| **Eclipse Temurin** | Eclipse Foundation's free, production-grade OpenJDK distribution |
| **Cache** | Storing downloaded dependencies between CI runs to avoid re-downloading them |
| **JAR** | Java ARchive — a packaged, deployable bundle of compiled Java classes and resources |
| **Artifact** | Any file produced by a build (JAR, test reports, coverage reports, etc.) |
| **Idempotent** | Running the build multiple times produces the same result — clean, reproducible builds |

---

<p align="center">
  <b>⚡ FinVault CI/CD Pipeline Document</b><br>
  <sub>Sprint 1 — SCRUM-12 (GitHub Actions Backend CI)</sub><br>
  <sub>Part of the <a href="ARCHITECTURE.md">FinVault Documentation Suite</a></sub>
</p>
