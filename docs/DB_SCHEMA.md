<p align="center">
  <img src="https://img.shields.io/badge/MySQL-8.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white" />
  <img src="https://img.shields.io/badge/Engine-InnoDB-00758F?style=for-the-badge" />
  <img src="https://img.shields.io/badge/Charset-utf8mb4-FF6B6B?style=for-the-badge" />
  <img src="https://img.shields.io/badge/Normal%20Form-3NF-28A745?style=for-the-badge" />
  <img src="https://img.shields.io/badge/Tables-3-blue?style=for-the-badge" />
</p>

# 🗄️ FinVault — Relational Database Schema

> **Database:** `finvault_db` | **Engine:** MySQL 8.0 (InnoDB) | **Charset:** `utf8mb4` | **Collation:** `utf8mb4_unicode_ci`

---

## 📑 Table of Contents

| # | Section |
|:-:|---------|
| 1 | [Database Theory](#-database-theory) |
| 2 | [ACID Guarantees](#-acid-guarantees) |
| 3 | [Normalization (3NF)](#-normalization-3nf) |
| 4 | [Entity Relationship Diagram](#-entity-relationship-diagram) |
| 5 | [Table: `users`](#-table-users) |
| 6 | [Table: `virtual_cards`](#-table-virtual_cards) |
| 7 | [Table: `transactions`](#-table-transactions) |
| 8 | [Constraints & Indexes](#-constraints--indexes) |
| 9 | [Full SQL Schema Script](#-full-sql-schema-script) |
| 10 | [JPA ↔ SQL Field Mapping](#-jpa--sql-field-mapping) |

---

## 📖 Database Theory

### Why MySQL for a Financial Application?

| Feature | Benefit for FinVault |
|---------|---------------------|
| **ACID Transactions** | Guarantees financial data integrity |
| **InnoDB Engine** | Row-level locking, crash recovery, FK support |
| **Open Source** | Free, massive community, well-documented |
| **Spring Boot Support** | First-class integration via `spring-boot-starter-data-jpa` |
| **Industry Standard** | Used by 39% of developers (Stack Overflow Survey) |

---

## 🔐 ACID Guarantees

> ACID is the gold standard for database transactions — **critical** in financial applications:

| Property | Meaning | FinVault Example |
|:--------:|---------|------------------|
| **A** — Atomicity | All-or-nothing | Transaction + balance update either both succeed or both roll back |
| **C** — Consistency | Data always valid | Card `status` can only be ACTIVE, FROZEN, EXPIRED, or CANCELLED |
| **I** — Isolation | No interference | Two concurrent transactions against the same card don't corrupt the balance |
| **D** — Durability | Survives crashes | Once a transaction is committed, it exists permanently |

### 🎓 Interview Tip

> "FinVault uses `@Transactional` on service methods to ensure atomicity — if the balance update succeeds but saving the transaction record fails, the entire operation rolls back."

---

## 📐 Normalization (3NF)

FinVault's schema follows **Third Normal Form** — the industry standard for transactional databases:

| Normal Form | Rule | FinVault Compliance |
|:-----------:|------|---------------------|
| **1NF** | Each column holds a single atomic value | ✅ No CSV or JSON columns |
| **2NF** | Every non-key column depends on the full primary key | ✅ No composite keys — all tables use surrogate `id` |
| **3NF** | No transitive dependencies between non-key columns | ✅ Username lives in `users`, not repeated in `virtual_cards` |

### Bad vs Good Design

```
❌ BAD (Denormalized):
┌──────────────────────────────────────────────────┐
│ card_id │ card_number │ user_name │ user_email    │
│    1    │ 4111...1111 │ johndoe   │ john@ex.      │
│    2    │ 4222...2222 │ johndoe   │ john@ex.      │ ← "johndoe" duplicated!
└──────────────────────────────────────────────────┘

✅ GOOD (3NF — FinVault):
┌──────────────────┐     ┌──────────────────────────┐
│ users            │     │ virtual_cards            │
│ id │ username    │     │ id │ user_id (FK)        │
│  1 │ johndoe     │◄────│  1 │ 1                   │
│    │             │     │  2 │ 1                   │
└──────────────────┘     └──────────────────────────┘
```

---

## 🔗 Entity Relationship Diagram

```
╔═══════════════════════════════════════════════════════════════════════════╗
║                     ENTITY RELATIONSHIP DIAGRAM                          ║
╠═══════════════════════════════════════════════════════════════════════════╣
║                                                                           ║
║   ┌───────────────────────┐       ┌────────────────────────────────┐     ║
║   │       📋 users         │       │      💳 virtual_cards          │     ║
║   ├───────────────────────┤       ├────────────────────────────────┤     ║
║   │ 🔑 id (PK, BIGINT AI) │       │ 🔑 id (PK, BIGINT AI)         │     ║
║   │    username (UQ)      │ 1   M │ 🔗 user_id (FK → users.id)    │     ║
║   │    email (UQ)         │◄──────│    card_number (UQ, CHAR 16)  │     ║
║   │    password_hash      │       │    expiry_date (DATE)         │     ║
║   │    created_at         │       │    cvv (CHAR 3)               │     ║
║   └───────────────────────┘       │    daily_limit (DECIMAL 10,2) │     ║
║                                    │    balance (DECIMAL 10,2)     │     ║
║                                    │    status (ENUM)              │     ║
║                                    │    vendor_name (VARCHAR 100)  │     ║
║                                    │    created_at                 │     ║
║                                    └──────────────┬───────────────┘     ║
║                                                   │                      ║
║                                              1    │   M                  ║
║                                                   │                      ║
║                                    ┌──────────────▼───────────────┐     ║
║                                    │      📊 transactions          │     ║
║                                    ├──────────────────────────────┤     ║
║                                    │ 🔑 id (PK, BIGINT AI)        │     ║
║                                    │ 🔗 virtual_card_id (FK)       │     ║
║                                    │    amount (DECIMAL 10,2)     │     ║
║                                    │    merchant_name (VARCHAR 100)│     ║
║                                    │    timestamp (DATETIME)      │     ║
║                                    │    status (ENUM)             │     ║
║                                    └──────────────────────────────┘     ║
║                                                                           ║
║   RELATIONSHIPS:                                                          ║
║   • One User has Many VirtualCards (1:M)                                  ║
║   • One VirtualCard has Many Transactions (1:M)                           ║
║   • CASCADE: Delete user → delete cards → delete transactions             ║
║                                                                           ║
╚═══════════════════════════════════════════════════════════════════════════╝
```

---

## 👤 Table: `users`

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | `BIGINT` | PK, AUTO_INCREMENT | Surrogate primary key |
| `username` | `VARCHAR(50)` | NOT NULL, UNIQUE | Display name |
| `email` | `VARCHAR(100)` | NOT NULL, UNIQUE | Login identifier |
| `password_hash` | `VARCHAR(60)` | NOT NULL | BCrypt hash (never plaintext) |
| `created_at` | `DATETIME` | NOT NULL | Auto-set by `@PrePersist` |

### Design Decisions

| Decision | Rationale |
|----------|-----------|
| `BIGINT` for PK | Supports billions of records; compatible with `GenerationType.IDENTITY` |
| `VARCHAR(60)` for hash | BCrypt always produces exactly 60 characters |
| `UNIQUE` on email + username | Prevents duplicate accounts at the DB level (defense-in-depth) |
| No `role` column yet | Simplified for training scope; will add when RBAC is needed |

---

## 💳 Table: `virtual_cards`

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | `BIGINT` | PK, AUTO_INCREMENT | Surrogate primary key |
| `user_id` | `BIGINT` | NOT NULL, FK → `users.id` | Owning user |
| `card_number` | `CHAR(16)` | NOT NULL, UNIQUE | Random 16-digit number |
| `expiry_date` | `DATE` | NOT NULL | Card validity (now + 3 years) |
| `cvv` | `CHAR(3)` | NOT NULL | Random 3-digit verification code |
| `daily_limit` | `DECIMAL(10,2)` | NOT NULL, DEFAULT 0.00 | Maximum spend per day |
| `balance` | `DECIMAL(10,2)` | NOT NULL, DEFAULT 0.00 | Running total spent today |
| `status` | `ENUM('ACTIVE','FROZEN','EXPIRED','CANCELLED')` | NOT NULL | Card lifecycle state |
| `vendor_name` | `VARCHAR(100)` | NOT NULL, DEFAULT '' | Human-readable vendor/purpose label |
| `created_at` | `DATETIME` | NOT NULL | Auto-set by `@PrePersist` |

### Design Decisions

| Decision | Rationale |
|----------|-----------|
| `CHAR(16)` for card_number | Fixed-length — always exactly 16 digits; more efficient than VARCHAR |
| `DECIMAL(10,2)` for money | Exact arithmetic; no floating-point precision loss |
| `ENUM` for status | DB-enforced domain constraint — only valid states can be stored |
| `vendor_name` DEFAULT '' | Backward-compatible migration — existing rows get empty string |
| `balance` column | Running total avoids re-summing all transactions on every request |
| FK with CASCADE | Deleting a user automatically removes their cards |

### 🎓 Why `balance` Instead of Calculating from Transactions? (Interview)

> Storing the running balance is a **performance optimization**. Summing all transactions on every API call would require a `GROUP BY` query that grows slower as transactions accumulate. A denormalized `balance` column provides O(1) reads at the cost of slightly more complex writes.

---

## 📊 Table: `transactions`

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | `BIGINT` | PK, AUTO_INCREMENT | Surrogate primary key |
| `virtual_card_id` | `BIGINT` | NOT NULL, FK → `virtual_cards.id` | Card charged |
| `amount` | `DECIMAL(10,2)` | NOT NULL | Transaction amount |
| `merchant_name` | `VARCHAR(100)` | NOT NULL | Where the spend occurred |
| `timestamp` | `DATETIME` | NOT NULL | Auto-set by `@PrePersist` |
| `status` | `ENUM('SUCCESS','DECLINED')` | NOT NULL | Approval outcome |

### Design Decisions

| Decision | Rationale |
|----------|-----------|
| Both SUCCESS and DECLINED are stored | Complete audit trail — compliance requirement for financial systems |
| FK to `virtual_cards` (not `users`) | Transactions belong to a card, which belongs to a user — proper normalization |
| `CASCADE` on FK | Deleting a card removes its transactions — no orphaned rows |
| No `card_number` column | Would violate 3NF; join via FK when display is needed |

---

## 🔧 Constraints & Indexes

| Constraint Type | Table | Column(s) | Purpose |
|:---------------:|-------|-----------|---------|
| PRIMARY KEY | all | `id` | Unique row identifier + clustered index |
| FOREIGN KEY | `virtual_cards` | `user_id → users.id` | Referential integrity + CASCADE DELETE |
| FOREIGN KEY | `transactions` | `virtual_card_id → virtual_cards.id` | Referential integrity + CASCADE DELETE |
| UNIQUE | `users` | `username` | Prevent duplicate usernames |
| UNIQUE | `users` | `email` | Prevent duplicate accounts |
| UNIQUE | `virtual_cards` | `card_number` | Prevent duplicate card numbers |
| NOT NULL | all columns | — | All fields are mandatory |
| ENUM | `virtual_cards` | `status` | Only valid lifecycle states |
| ENUM | `transactions` | `status` | Only SUCCESS or DECLINED |

---

## 📜 Full SQL Schema Script

```sql
-- ============================================================
-- FinVault Database Initialization Script
-- Engine: MySQL 8.0 | Charset: utf8mb4 | Collation: utf8mb4_unicode_ci
-- ============================================================

CREATE DATABASE IF NOT EXISTS finvault_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE finvault_db;

-- ──────────────────────────────────────────────────────────────
-- Table 1: users
-- ──────────────────────────────────────────────────────────────
CREATE TABLE users (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    username        VARCHAR(50)     NOT NULL,
    email           VARCHAR(100)    NOT NULL,
    password_hash   VARCHAR(60)     NOT NULL,
    created_at      DATETIME        NOT NULL,

    PRIMARY KEY (id),
    UNIQUE KEY uk_users_username (username),
    UNIQUE KEY uk_users_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ──────────────────────────────────────────────────────────────
-- Table 2: virtual_cards
-- ──────────────────────────────────────────────────────────────
CREATE TABLE virtual_cards (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    user_id         BIGINT          NOT NULL,
    card_number     CHAR(16)        NOT NULL,
    expiry_date     DATE            NOT NULL,
    cvv             CHAR(3)         NOT NULL,
    daily_limit     DECIMAL(10,2)   NOT NULL DEFAULT 0.00,
    balance         DECIMAL(10,2)   NOT NULL DEFAULT 0.00,
    status          ENUM('ACTIVE','FROZEN','EXPIRED','CANCELLED') NOT NULL DEFAULT 'ACTIVE',
    vendor_name     VARCHAR(100)    NOT NULL DEFAULT '',
    created_at      DATETIME        NOT NULL,

    PRIMARY KEY (id),
    UNIQUE KEY uk_cards_number (card_number),
    CONSTRAINT fk_cards_user FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ──────────────────────────────────────────────────────────────
-- Table 3: transactions
-- ──────────────────────────────────────────────────────────────
CREATE TABLE transactions (
    id                  BIGINT          NOT NULL AUTO_INCREMENT,
    virtual_card_id     BIGINT          NOT NULL,
    amount              DECIMAL(10,2)   NOT NULL,
    merchant_name       VARCHAR(100)    NOT NULL,
    timestamp           DATETIME        NOT NULL,
    status              ENUM('SUCCESS','DECLINED') NOT NULL,

    PRIMARY KEY (id),
    CONSTRAINT fk_tx_card FOREIGN KEY (virtual_card_id)
        REFERENCES virtual_cards(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

> **Note:** This script is for reference. FinVault uses `spring.jpa.hibernate.ddl-auto=update`, so Hibernate auto-creates/modifies tables on startup.

---

## 🔄 JPA ↔ SQL Field Mapping

### users ↔ User.java

| Java Field | JPA Annotation | SQL Column |
|-----------|---------------|------------|
| `Long id` | `@Id @GeneratedValue(IDENTITY)` | `id BIGINT PK AI` |
| `String username` | `@Column(unique=true, length=50)` | `username VARCHAR(50) UQ NN` |
| `String email` | `@Column(unique=true, length=100)` | `email VARCHAR(100) UQ NN` |
| `String passwordHash` | `@Column(name="password_hash", length=60)` | `password_hash VARCHAR(60) NN` |
| `LocalDateTime createdAt` | `@Column(updatable=false)` | `created_at DATETIME NN` |
| `List<VirtualCard> virtualCards` | `@OneToMany(mappedBy="user", cascade=ALL)` | *(no column — inverse side)* |

### virtual_cards ↔ VirtualCard.java

| Java Field | JPA Annotation | SQL Column |
|-----------|---------------|------------|
| `Long id` | `@Id @GeneratedValue(IDENTITY)` | `id BIGINT PK AI` |
| `User user` | `@ManyToOne(LAZY) @JoinColumn("user_id")` | `user_id BIGINT FK NN` |
| `String cardNumber` | `@Column(unique=true, length=16)` | `card_number CHAR(16) UQ NN` |
| `LocalDate expiryDate` | `@Column(name="expiry_date")` | `expiry_date DATE NN` |
| `String cvv` | `@Column(length=3)` | `cvv CHAR(3) NN` |
| `BigDecimal dailyLimit` | `@Column(precision=10, scale=2)` | `daily_limit DECIMAL(10,2) NN` |
| `BigDecimal balance` | `@Column(precision=10, scale=2)` | `balance DECIMAL(10,2) NN` |
| `CardStatus status` | `@Enumerated(STRING)` | `status ENUM(...) NN` |
| `String vendorName` | `@Column(columnDefinition="VARCHAR(100) NOT NULL DEFAULT ''")` | `vendor_name VARCHAR(100) NN` |
| `List<Transaction> transactions` | `@OneToMany(mappedBy="virtualCard", cascade=ALL)` | *(no column — inverse side)* |
| `LocalDateTime createdAt` | `@Column(updatable=false)` | `created_at DATETIME NN` |

### transactions ↔ Transaction.java

| Java Field | JPA Annotation | SQL Column |
|-----------|---------------|------------|
| `Long id` | `@Id @GeneratedValue(IDENTITY)` | `id BIGINT PK AI` |
| `VirtualCard virtualCard` | `@ManyToOne(LAZY) @JoinColumn("virtual_card_id")` | `virtual_card_id BIGINT FK NN` |
| `BigDecimal amount` | `@Column(precision=10, scale=2)` | `amount DECIMAL(10,2) NN` |
| `String merchantName` | `@Column(length=100)` | `merchant_name VARCHAR(100) NN` |
| `LocalDateTime timestamp` | `@Column(updatable=false)` | `timestamp DATETIME NN` |
| `TransactionStatus status` | `@Enumerated(STRING)` | `status ENUM('SUCCESS','DECLINED') NN` |

---

<p align="center">
  <b>🗄️ FinVault Database Schema Documentation</b><br>
  <sub>3 tables | 3NF | ACID | InnoDB | Cascading deletes</sub><br>
  <sub>Part of the <a href="../README.md">FinVault Documentation Suite</a></sub>
</p>
