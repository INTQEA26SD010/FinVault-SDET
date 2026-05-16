<p align="center">
  <img src="https://img.shields.io/badge/MySQL-8.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white" />
  <img src="https://img.shields.io/badge/Engine-InnoDB-00758F?style=for-the-badge" />
  <img src="https://img.shields.io/badge/Charset-utf8mb4-FF6B6B?style=for-the-badge" />
  <img src="https://img.shields.io/badge/Normal%20Form-3NF-28A745?style=for-the-badge" />
</p>

# 🗄️ FinVault — Relational Database Schema

> **Database:** `finvault_db` | **Engine:** MySQL 8.0 | **Charset:** `utf8mb4` | **Collation:** `utf8mb4_unicode_ci`

---

## 📑 Table of Contents

| # | Section | Description |
|:-:|---------|-------------|
| 1 | [Database Theory — What & Why?](#-database-theory--what--why) | Foundational concepts of relational databases |
| 2 | [Normalization — Designing Clean Tables](#-normalization--designing-clean-tables) | Why we normalize and what 3NF means |
| 3 | [Design Principles Applied](#-design-principles-applied) | FinVault's specific design philosophy |
| 4 | [Entity Relationship Diagram](#-entity-relationship-diagram) | Visual map of table relationships |
| 5 | [Table: `users`](#-table-users) | User accounts and authentication |
| 6 | [Table: `virtual_cards`](#-table-virtual_cards) | Virtual smart cards schema |
| 7 | [Table: `transactions`](#-table-transactions) | Transaction audit log |
| 8 | [Constraints Deep Dive](#-constraints-deep-dive) | Understanding PKs, FKs, UNIQUE, and ENUMs |
| 9 | [Full SQL Schema Script](#-full-sql-schema-script) | Ready-to-run initialization script |
| 10 | [JPA Mapping Preview](#-jpa-mapping-preview) | How tables map to Java entities |
| 11 | [Glossary](#-glossary) | Key database terms |

---

## 📖 Database Theory — What & Why?

### What is a Relational Database?

A **relational database** organizes data into **tables** (also called *relations*) that consist of **rows** (records) and **columns** (fields). Tables can be linked together using **keys**, which is what makes it "relational."

```
┌──────────────────────────────────────────────────────┐
│                    TABLE: users                       │
├──────┬───────────┬──────────────────┬───────────────┤
│  id  │ username  │ email            │ password_hash │
├──────┼───────────┼──────────────────┼───────────────┤
│  1   │ johndoe   │ john@example.com │ $2a$10$...    │
│  2   │ janedoe   │ jane@example.com │ $2a$10$...    │
│  3   │ bobrsmith │ bob@example.com  │ $2a$10$...    │
└──────┴───────────┴──────────────────┴───────────────┘
   ▲                                                    
   │  This is a TABLE with 3 ROWS and 4 COLUMNS        
```

### Why MySQL?

| Feature | Benefit for FinVault |
|---------|---------------------|
| **ACID Transactions** | Guarantees financial data integrity (Atomicity, Consistency, Isolation, Durability) |
| **InnoDB Engine** | Row-level locking, crash recovery, foreign key support |
| **Open Source** | Free to use, massive community, well-documented |
| **Spring Boot Support** | First-class integration via `spring-boot-starter-data-jpa` |
| **Industry Standard** | Used by Facebook, Twitter, YouTube, and 39% of all developers (Stack Overflow Survey) |

### What is ACID?

> 💡 ACID is the gold standard for database transactions, especially critical in **financial applications** like FinVault:

| Property | Meaning | FinVault Example |
|:--------:|---------|------------------|
| **A** — Atomicity | All-or-nothing — a transaction either fully completes or fully rolls back | User registration must save both the user AND hash the password — if hashing fails, no user is created |
| **C** — Consistency | Data always moves from one valid state to another | A card's `status` can only be `ACTIVE`, `FROZEN`, `EXPIRED`, or `CANCELLED` — never `DELETED` |
| **I** — Isolation | Concurrent transactions don't interfere with each other | Two users registering simultaneously won't get the same `id` |
| **D** — Durability | Once committed, data survives crashes | After a successful registration, the user exists even if the server crashes 1ms later |

---

## 📐 Normalization — Designing Clean Tables

### What is Normalization?

**Normalization** is the process of organizing database columns and tables to:
- ❌ **Eliminate data redundancy** (no duplicate data)
- ✅ **Ensure data integrity** (no contradictory data)
- ✅ **Simplify queries** (clean, predictable structure)

### The Normal Forms (Simplified)

| Normal Form | Rule | FinVault Example |
|:-----------:|------|------------------|
| **1NF** | Each column holds a single value (no arrays/lists) | ✅ `email` column holds ONE email, not a comma-separated list |
| **2NF** | Every non-key column depends on the **full** primary key | ✅ In `virtual_cards`, all columns depend on `id`, not on part of a composite key |
| **3NF** | No column depends on another non-key column (no transitive dependencies) | ✅ We don't store `username` in `virtual_cards` — we JOIN via `user_id` FK |

> 🎯 **FinVault's schema follows 3NF** — the industry standard for transactional systems.

### Bad Design vs Good Design

```
❌ BAD (Denormalized — data redundancy):
┌────────────────────────────────────────────────────────────┐
│ card_id │ card_number │ user_name │ user_email │ daily_limit│
│    1    │ 4111...1111 │ johndoe   │ john@ex.   │   500.00  │
│    2    │ 4222...2222 │ johndoe   │ john@ex.   │   200.00  │  ← "johndoe" repeated!
└────────────────────────────────────────────────────────────┘
Problem: If John changes his email, you must update EVERY row.

✅ GOOD (Normalized — 3NF):
┌──────────────────────┐     ┌──────────────────────────────┐
│ users                │     │ virtual_cards                │
│ id │ name  │ email   │     │ id │ user_id │ card_number   │
│  1 │ john  │ john@.. │◄────│  1 │   1     │ 4111...1111   │
│    │       │         │     │  2 │   1     │ 4222...2222   │
└──────────────────────┘     └──────────────────────────────┘
Fix: Change email ONCE in `users` — all cards automatically reference the updated data.
```

---

## 🎯 Design Principles Applied

FinVault's schema follows these deliberate design principles:

| Principle | Implementation | Why It Matters |
|-----------|---------------|----------------|
| **Surrogate Primary Keys** | `BIGINT AUTO_INCREMENT` on every table | Decouples business data from row identity; compatible with Hibernate's `GenerationType.IDENTITY` |
| **Password Hashing** | `password_hash` stores BCrypt output (60 chars) | Raw passwords are **NEVER** stored — security best practice |
| **Audit Timestamps** | `created_at DEFAULT CURRENT_TIMESTAMP` | Database captures record creation time independently of the application layer |
| **Foreign Key Constraints** | `virtual_cards.user_id → users.id` with `ON DELETE CASCADE` | Deleting a user automatically purges all their cards (no orphaned rows) |
| **Domain-Driven Types** | `CHAR(16)` for card number, `CHAR(3)` for CVV | Fixed-length types enforce exact data formats at the database level |
| **ENUM Constraints** | `status ENUM('ACTIVE','FROZEN','EXPIRED','CANCELLED')` | Only valid states can be stored — enforced by the database engine |

---

## 🔗 Entity Relationship Diagram

```
╔══════════════════════════════════════════════════════════════════╗
║                     ENTITY RELATIONSHIP DIAGRAM                  ║
╠══════════════════════════════════════════════════════════════════╣
║                                                                  ║
║   ┌─────────────────────┐          ┌──────────────────────────┐  ║
║   │      📋 users        │          │   💳 virtual_cards       │  ║
║   ├─────────────────────┤          ├──────────────────────────┤  ║
║   │ 🔑 id (PK)          │          │ 🔑 id (PK)              │  ║
║   │    username (UQ)    │ 1    M   │ 🔗 user_id (FK) ────────│──╫──► users.id
║   │    email (UQ)       │◄─────────│    card_number (UQ)     │  ║
║   │    password_hash    │          │    expiry_date          │  ║
║   │    created_at       │          │    cvv                  │  ║
║   └─────────────────────┘          │    daily_limit          │  ║
║                                     │    status (ENUM)        │  ║
║                                     │    created_at           │  ║
║      ONE user has MANY cards        └──────────────────────────┘  ║
║      Each card belongs to ONE user                               ║
║                                                                  ║
║   Relationship: users (1) ───────────── (Many) virtual_cards     ║
║   Cascade: ON DELETE CASCADE (delete user → delete all cards)    ║
║                                                                  ║
╚══════════════════════════════════════════════════════════════════╝
```

---

## 👤 Table: `users`

> Stores the core identity and authentication credentials for every FinVault account holder.

### SQL Definition

```sql
CREATE TABLE users (
    id            BIGINT          NOT NULL AUTO_INCREMENT,
    username      VARCHAR(50)     NOT NULL,
    email         VARCHAR(100)    NOT NULL,
    password_hash VARCHAR(60)     NOT NULL  COMMENT 'BCrypt hash — never store plaintext',
    created_at    DATETIME        NOT NULL  DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_users          PRIMARY KEY (id),
    CONSTRAINT uq_users_email    UNIQUE      (email),
    CONSTRAINT uq_users_username UNIQUE      (username)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='Core user accounts and authentication credentials';
```

### Column Reference

| Column | Type | Constraints | Description |
|:------:|:----:|:-----------:|-------------|
| `id` | `BIGINT` | `PK`, `AUTO_INCREMENT` | Surrogate key — maps to `@GeneratedValue(strategy = IDENTITY)` in JPA |
| `username` | `VARCHAR(50)` | `NOT NULL`, `UNIQUE` | Display name, used for login identification |
| `email` | `VARCHAR(100)` | `NOT NULL`, `UNIQUE` | Primary contact — ensures no duplicate registrations |
| `password_hash` | `VARCHAR(60)` | `NOT NULL` | BCrypt output — always exactly 60 characters (e.g., `$2a$10$N9qo8...`) |
| `created_at` | `DATETIME` | `NOT NULL`, `DEFAULT NOW()` | Auto-set by MySQL on `INSERT` — audit trail |

### Why VARCHAR(60) for password_hash?

BCrypt always produces a **fixed-length 60-character string** in this format:

```
$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
 ││  ││ └──────────────────── 53 chars: salt + hash ────────────┘
 ││  │└──── Cost factor (10 = 2^10 iterations)
 ││  └───── Version separator
 │└──────── Algorithm version (2a)
 └───────── BCrypt identifier
```

---

## 💳 Table: `virtual_cards`

> Stores the virtual smart cards issued to users. Each card acts as a **spending firewall** with its own daily limit and freeze capability.

### SQL Definition

```sql
CREATE TABLE virtual_cards (
    id          BIGINT           NOT NULL AUTO_INCREMENT,
    user_id     BIGINT           NOT NULL               COMMENT 'FK → users.id',
    card_number CHAR(16)         NOT NULL,
    expiry_date DATE             NOT NULL               COMMENT 'Format: YYYY-MM-DD (last day of month)',
    cvv         CHAR(3)          NOT NULL,
    daily_limit DECIMAL(10, 2)   NOT NULL  DEFAULT 0.00 COMMENT 'Max spend per day in base currency',
    vendor_name VARCHAR(100)      NOT NULL  DEFAULT ''   COMMENT 'Vendor or purpose label (e.g. Amazon, Netflix)',
    status      ENUM(
                    'ACTIVE',
                    'FROZEN',
                    'EXPIRED',
                    'CANCELLED'
                )                NOT NULL  DEFAULT 'ACTIVE',
    created_at  DATETIME         NOT NULL  DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_virtual_cards         PRIMARY KEY (id),
    CONSTRAINT uq_virtual_cards_number  UNIQUE      (card_number),
    CONSTRAINT fk_virtual_cards_user    FOREIGN KEY (user_id)
        REFERENCES users (id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='Virtual smart cards issued to users — acts as per-user spending firewall';
```

---

## 📊 Table: `transactions`

> Records every spend attempt against a VirtualCard — both approved and declined. Provides the complete audit trail for the Transactions tab in the dashboard.

### SQL Definition

```sql
CREATE TABLE transactions (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    virtual_card_id BIGINT          NOT NULL               COMMENT 'FK → virtual_cards.id',
    amount          DECIMAL(10, 2)  NOT NULL               COMMENT 'Spend amount',
    merchant_name   VARCHAR(100)    NOT NULL,
    timestamp       DATETIME        NOT NULL               COMMENT 'Set by @PrePersist in Transaction.java',
    status          ENUM(
                        'SUCCESS',
                        'DECLINED'
                    )               NOT NULL,

    CONSTRAINT pk_transactions          PRIMARY KEY (id),
    CONSTRAINT fk_transactions_card     FOREIGN KEY (virtual_card_id)
        REFERENCES virtual_cards (id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='Audit log of every spend attempt — SUCCESS or DECLINED';
```

### Column Reference

| Column | Type | Constraints | Description |
|:------:|:----:|:-----------:|-------------|
| `id` | `BIGINT` | `PK`, `AUTO_INCREMENT` | Surrogate key |
| `virtual_card_id` | `BIGINT` | `NOT NULL`, `FK → virtual_cards.id` | Which card was charged |
| `amount` | `DECIMAL(10,2)` | `NOT NULL` | Exact monetary amount — `DECIMAL` for precision |
| `merchant_name` | `VARCHAR(100)` | `NOT NULL` | Name of the merchant (e.g., "Coffee Shop") |
| `timestamp` | `DATETIME` | `NOT NULL` | Auto-set by `@PrePersist` in `Transaction.java`; records exact time |
| `status` | `ENUM` | `NOT NULL` | `SUCCESS` — balance updated; `DECLINED` — limit exceeded, balance unchanged |

### Approval Logic (enforced in `TransactionService`)

```
projectedBalance = card.balance + request.amount

IF projectedBalance <= card.dailyLimit  →  SUCCESS (balance updated)
IF projectedBalance > card.dailyLimit   →  DECLINED (balance unchanged)

Both outcomes are persisted as a Transaction row for audit trail.
```

> ⚠️ Every transaction is saved regardless of outcome. This enables the dashboard's Transactions tab to show both approved and declined history.

### Column Reference

| Column | Type | Constraints | Description |
|:------:|:----:|:-----------:|-------------|
| `id` | `BIGINT` | `PK`, `AUTO_INCREMENT` | Surrogate key — auto-generated by MySQL |
| `user_id` | `BIGINT` | `NOT NULL`, `FK → users.id` | Many-to-one relationship; cascades on delete |
| `card_number` | `CHAR(16)` | `NOT NULL`, `UNIQUE` | Fixed 16-digit virtual card number (e.g., `4111111111111111`) |
| `expiry_date` | `DATE` | `NOT NULL` | Stored as `YYYY-MM-DD`; represents last valid day of the card |
| `cvv` | `CHAR(3)` | `NOT NULL` | 3-digit Card Verification Value — **never exposed in API responses** |
| `daily_limit` | `DECIMAL(10,2)` | `NOT NULL`, `DEFAULT 0.00` | Max daily spend — uses `DECIMAL` for cent-level precision (no `FLOAT`!) |
| `vendor_name` | `VARCHAR(100)` | `NOT NULL`, `DEFAULT ''` | Human-readable label for the card's purpose or linked vendor (e.g. `Amazon`) |
| `status` | `ENUM` | `NOT NULL`, `DEFAULT 'ACTIVE'` | Card lifecycle state (see table below) |
| `created_at` | `DATETIME` | `NOT NULL`, `DEFAULT NOW()` | Auto-set on row creation — audit trail |

### Card Status Lifecycle

```
                    ┌──────────┐
                    │  ACTIVE  │ ← Default state when card is created
                    └────┬─────┘
                         │
              ┌──────────┼──────────┐
              ▼          │          ▼
        ┌──────────┐    │    ┌───────────┐
        │  FROZEN  │    │    │  EXPIRED  │ ← Auto-set when expiry_date passes
        └────┬─────┘    │    └───────────┘
             │          │
             ▼          ▼
        ┌──────────┐  ┌───────────┐
        │  ACTIVE  │  │ CANCELLED │ ← Permanent — cannot be reversed
        │ (unfroze)│  └───────────┘
        └──────────┘
```

| Status | Meaning | Can Transact? | Reversible? |
|:------:|---------|:-------------:|:-----------:|
| `ACTIVE` | Card is live and usable | ✅ Yes | — |
| `FROZEN` | Temporarily suspended by user | ❌ No | ✅ Yes → ACTIVE |
| `EXPIRED` | Past the expiry date | ❌ No | ❌ No |
| `CANCELLED` | Permanently deactivated | ❌ No | ❌ No |

### Why DECIMAL Instead of FLOAT for Money?

> ⚠️ **Never use `FLOAT` or `DOUBLE` for money!**

```
-- FLOAT (imprecise):
SELECT 0.1 + 0.2;          →  0.30000000000000004  ❌

-- DECIMAL(10,2) (exact):
SELECT 0.10 + 0.20;        →  0.30                  ✅
```

`DECIMAL(10,2)` means:
- **10** total digits maximum
- **2** digits after the decimal point
- Range: `-99999999.99` to `99999999.99`
- Perfect for currency values — no rounding surprises

---

## 🔐 Constraints Deep Dive

### What Are Database Constraints?

Constraints are **rules enforced by the database engine** that prevent invalid data from being stored. They are the last line of defense — even if the application has a bug, the database will reject bad data.

| Constraint Type | Symbol | Purpose | FinVault Usage |
|:---------------:|:------:|---------|----------------|
| **PRIMARY KEY** | `PK` | Uniquely identifies each row | `users.id`, `virtual_cards.id` |
| **FOREIGN KEY** | `FK` | Links a column to another table's PK | `virtual_cards.user_id → users.id` |
| **UNIQUE** | `UQ` | Ensures no duplicate values in a column | `users.email`, `users.username`, `virtual_cards.card_number` |
| **NOT NULL** | `NN` | Column cannot be empty/null | Applied to every column in both tables |
| **DEFAULT** | `DF` | Auto-fills a value if none is provided | `status = 'ACTIVE'`, `daily_limit = 0.00`, `created_at = NOW()` |
| **CHECK (ENUM)** | `CK` | Restricts values to a predefined set | `status IN ('ACTIVE','FROZEN','EXPIRED','CANCELLED')` |

### CASCADE Explained

```sql
FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE ON UPDATE CASCADE
```

| Action | What Happens | Example |
|--------|-------------|---------|
| `ON DELETE CASCADE` | Deleting a parent row **automatically deletes** all child rows | Delete user #1 → all of user #1's cards are deleted too |
| `ON UPDATE CASCADE` | Changing a parent PK **automatically updates** all child FKs | If user #1's id changes to #99, all cards' `user_id` changes to 99 too |

> 💡 **Why CASCADE?** Without it, trying to delete a user who has cards would throw a `foreign key constraint violation` error, leaving you stuck with orphaned data.

---

## 📜 Full SQL Schema Script

> Run this script **once** against your local MySQL instance to initialize the database.  
> **Pre-requisite:** `CREATE DATABASE IF NOT EXISTS finvault_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;`

```sql
-- =================================================================
-- FinVault Database Schema — v1.0 (Sprint 1)
-- Engine: MySQL 8.0 | Charset: utf8mb4 | Collation: utf8mb4_unicode_ci
-- =================================================================

USE finvault_db;

-- -----------------------------------------------------------------
-- Table 1: users
-- Purpose: Core user accounts and authentication credentials
-- -----------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id            BIGINT          NOT NULL AUTO_INCREMENT,
    username      VARCHAR(50)     NOT NULL,
    email         VARCHAR(100)    NOT NULL,
    password_hash VARCHAR(60)     NOT NULL  COMMENT 'BCrypt hash — never store plaintext',
    created_at    DATETIME        NOT NULL  DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_users          PRIMARY KEY  (id),
    CONSTRAINT uq_users_email    UNIQUE       (email),
    CONSTRAINT uq_users_username UNIQUE       (username)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='Core user accounts and authentication credentials';

-- -----------------------------------------------------------------
-- Table 3: transactions
-- Purpose: Audit log of every spend attempt against a virtual card
-- Depends on: virtual_cards (FK: virtual_card_id → virtual_cards.id)
-- -----------------------------------------------------------------
CREATE TABLE IF NOT EXISTS transactions (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    virtual_card_id BIGINT          NOT NULL               COMMENT 'FK → virtual_cards.id',
    amount          DECIMAL(10, 2)  NOT NULL               COMMENT 'Spend amount',
    merchant_name   VARCHAR(100)    NOT NULL,
    timestamp       DATETIME        NOT NULL               COMMENT 'Set by @PrePersist in Transaction.java',
    status          ENUM(
                        'SUCCESS',
                        'DECLINED'
                    )               NOT NULL,

    CONSTRAINT pk_transactions          PRIMARY KEY (id),
    CONSTRAINT fk_transactions_card     FOREIGN KEY (virtual_card_id)
        REFERENCES virtual_cards (id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='Audit log of every spend attempt — SUCCESS or DECLINED';
    id          BIGINT           NOT NULL AUTO_INCREMENT,
    user_id     BIGINT           NOT NULL               COMMENT 'FK → users.id',
    card_number CHAR(16)         NOT NULL,
    expiry_date DATE             NOT NULL               COMMENT 'Format: YYYY-MM-DD',
    cvv         CHAR(3)          NOT NULL,
    daily_limit DECIMAL(10, 2)   NOT NULL  DEFAULT 0.00 COMMENT 'Max spend per day in base currency',
    vendor_name VARCHAR(100)     NOT NULL  DEFAULT ''   COMMENT 'Vendor or purpose label (e.g. Amazon, Netflix)',
    status      ENUM(
                    'ACTIVE',
                    'FROZEN',
                    'EXPIRED',
                    'CANCELLED'
                )                NOT NULL  DEFAULT 'ACTIVE',
    created_at  DATETIME         NOT NULL  DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_virtual_cards        PRIMARY KEY (id),
    CONSTRAINT uq_virtual_cards_number UNIQUE      (card_number),
    CONSTRAINT fk_virtual_cards_user   FOREIGN KEY (user_id)
        REFERENCES users (id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='Virtual smart cards — per-user spending firewall';
```

---

## 🔮 JPA Mapping Preview

These SQL tables map directly to Java `@Entity` classes in the Spring Boot backend:

| SQL Table | Java Entity | Spring Data Repository | Details |
|:---------:|:-----------:|:---------------------:|---------|
| `users` | `User.java` | `UserRepository.java` | See [JPA_ENTITIES.md](JPA_ENTITIES.md) |
| `virtual_cards` | `VirtualCard.java` | `VirtualCardRepository.java` | See [JPA_ENTITIES.md](JPA_ENTITIES.md) |
| `transactions` | `Transaction.java` | `TransactionRepository.java` | See [JPA_ENTITIES.md](JPA_ENTITIES.md) |
### How JPA Maps SQL Types to Java Types

| SQL Column Type | Java Field Type | Why This Mapping? |
|:---------------:|:---------------:|-------------------|
| `BIGINT` | `Long` | 64-bit integer for large auto-increment IDs |
| `VARCHAR(n)` | `String` | Variable-length text |
| `CHAR(n)` | `String` | Fixed-length text (card numbers, CVV) |
| `DATETIME` | `LocalDateTime` | Java 8+ date/time without timezone — stored as-is in MySQL |
| `DATE` | `LocalDate` | Date-only (no time component) — for expiry dates |
| `DECIMAL(10,2)` | `BigDecimal` | Exact decimal arithmetic — mandatory for financial values |
| `ENUM(...)` | `enum CardStatus` | Type-safe in Java + `@Enumerated(STRING)` stores name, not ordinal |

---

## 📚 Glossary

| Term | Definition |
|------|-----------|
| **DDL** | Data Definition Language — SQL commands that define schema structure (`CREATE TABLE`, `ALTER TABLE`, `DROP TABLE`) |
| **DML** | Data Manipulation Language — SQL commands that manipulate data (`INSERT`, `SELECT`, `UPDATE`, `DELETE`) |
| **Primary Key (PK)** | A column (or set of columns) that uniquely identifies each row in a table |
| **Foreign Key (FK)** | A column that references the primary key of another table, creating a relationship |
| **Surrogate Key** | An artificial key (like `AUTO_INCREMENT id`) with no business meaning — used purely for row identification |
| **Natural Key** | A key made from real business data (like `email`) — has meaning but can change |
| **Normalization** | Organizing database tables to reduce redundancy and improve data integrity |
| **3NF** | Third Normal Form — no transitive dependencies between non-key columns |
| **CASCADE** | An action that automatically propagates changes from parent rows to child rows |
| **InnoDB** | MySQL's default storage engine — supports transactions, row-level locking, and foreign keys |
| **utf8mb4** | MySQL's full UTF-8 character set (supports emojis and all Unicode characters, unlike `utf8` which is limited to 3 bytes) |
| **ACID** | Atomicity, Consistency, Isolation, Durability — the four guarantees of reliable database transactions |

---

<p align="center">
  <b>🗄️ FinVault Database Schema Document</b><br>
  <sub>Sprint 1 — SCRUM-11 (Initial DB Schema — Users & Virtual Cards) | Sprint 2 — SCRUM-17 (Transactions Table) | Card Management — SCRUM-20 (vendor_name column, cascade delete)</sub><br>
  <sub>Part of the <a href="ARCHITECTURE.md">FinVault Documentation Suite</a></sub>
</p>
