# FinVault — Relational Database Schema

> **Database:** `finvault_db` | **Engine:** MySQL 8.0 | **Charset:** `utf8mb4` | **Collation:** `utf8mb4_unicode_ci`

---

## Overview

FinVault uses a **normalized relational schema** designed around two core entities for the initial sprint: **Users** and **Virtual Cards**. The schema follows third normal form (3NF) to eliminate data redundancy, enforce referential integrity, and support the domain model that will be mapped to JPA `@Entity` classes in the Spring Boot backend.

The design principles applied here are:

- **Surrogate primary keys** (`BIGINT AUTO_INCREMENT`) on every table — decouples business data from row identity and is compatible with Hibernate's `GenerationType.IDENTITY` strategy.
- **Password hashing** — raw passwords are never stored. The `password_hash` column stores the output of **BCrypt** (Spring Security's default), which produces a fixed 60-character string.
- **Audit timestamps** — `created_at` uses `DEFAULT CURRENT_TIMESTAMP` so the database layer captures record creation time independently of the application layer.
- **Foreign key constraints** — `virtual_cards.user_id` references `users.id` with `ON DELETE CASCADE`, ensuring that deleting a user automatically purges all their associated cards (no orphaned rows).
- **Domain-driven field types** — `card_number` is stored as `CHAR(16)` (always exactly 16 digits), `cvv` as `CHAR(3)`, and card `status` as an `ENUM` to enforce valid state values at the database level.

---

## Entity Relationship

```
users (1) ─────────────── (many) virtual_cards
   id  ◄──────────────────────── user_id (FK)
```

One user can own many virtual cards. Each card belongs to exactly one user.

---

## Table: `users`

Stores the core identity and authentication credentials for every FinVault account holder.

```sql
CREATE TABLE users (
    id            BIGINT          NOT NULL AUTO_INCREMENT,
    username      VARCHAR(50)     NOT NULL,
    email         VARCHAR(100)    NOT NULL,
    password_hash VARCHAR(60)     NOT NULL  COMMENT 'BCrypt hash — never store plaintext',
    created_at    DATETIME        NOT NULL  DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_users         PRIMARY KEY (id),
    CONSTRAINT uq_users_email   UNIQUE      (email),
    CONSTRAINT uq_users_username UNIQUE     (username)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='Core user accounts and authentication credentials';
```

### Column Reference

| Column | Type | Constraints | Notes |
|---|---|---|---|
| `id` | `BIGINT` | `PK`, `AUTO_INCREMENT` | Surrogate key; maps to `@GeneratedValue(strategy = IDENTITY)` |
| `username` | `VARCHAR(50)` | `NOT NULL`, `UNIQUE` | Display name, used for login |
| `email` | `VARCHAR(100)` | `NOT NULL`, `UNIQUE` | Primary contact; used for notifications |
| `password_hash` | `VARCHAR(60)` | `NOT NULL` | BCrypt output; fixed 60-char length |
| `created_at` | `DATETIME` | `NOT NULL`, `DEFAULT NOW()` | Auto-set by MySQL on `INSERT` |

---

## Table: `virtual_cards`

Stores the virtual smart cards issued to users. Each card acts as a spending firewall with its own daily limit and freeze capability.

```sql
CREATE TABLE virtual_cards (
    id          BIGINT           NOT NULL AUTO_INCREMENT,
    user_id     BIGINT           NOT NULL               COMMENT 'FK → users.id',
    card_number CHAR(16)         NOT NULL,
    expiry_date DATE             NOT NULL               COMMENT 'Format: YYYY-MM-DD (last day of month)',
    cvv         CHAR(3)          NOT NULL,
    daily_limit DECIMAL(10, 2)   NOT NULL  DEFAULT 0.00 COMMENT 'Max spend per day in base currency',
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

### Column Reference

| Column | Type | Constraints | Notes |
|---|---|---|---|
| `id` | `BIGINT` | `PK`, `AUTO_INCREMENT` | Surrogate key |
| `user_id` | `BIGINT` | `NOT NULL`, `FK → users.id` | Many-to-one relationship; cascades on delete |
| `card_number` | `CHAR(16)` | `NOT NULL`, `UNIQUE` | Fixed 16-digit virtual card number |
| `expiry_date` | `DATE` | `NOT NULL` | Stored as `YYYY-MM-DD`; represents last valid day |
| `cvv` | `CHAR(3)` | `NOT NULL` | 3-digit security code |
| `daily_limit` | `DECIMAL(10,2)` | `NOT NULL`, `DEFAULT 0.00` | Monetary value; 2 decimal places for cents precision |
| `status` | `ENUM` | `NOT NULL`, `DEFAULT 'ACTIVE'` | Valid states: `ACTIVE`, `FROZEN`, `EXPIRED`, `CANCELLED` |
| `created_at` | `DATETIME` | `NOT NULL`, `DEFAULT NOW()` | Auto-set on row creation |

---

## Full Schema Script (Run Order)

> Run this script once against your local MySQL instance to initialize the schema.  
> Pre-requisite: `CREATE DATABASE IF NOT EXISTS finvault_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;`

```sql
USE finvault_db;

-- -------------------------------------------------------
-- Table: users
-- -------------------------------------------------------
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

-- -------------------------------------------------------
-- Table: virtual_cards
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS virtual_cards (
    id          BIGINT           NOT NULL AUTO_INCREMENT,
    user_id     BIGINT           NOT NULL               COMMENT 'FK → users.id',
    card_number CHAR(16)         NOT NULL,
    expiry_date DATE             NOT NULL               COMMENT 'Format: YYYY-MM-DD',
    cvv         CHAR(3)          NOT NULL,
    daily_limit DECIMAL(10, 2)   NOT NULL  DEFAULT 0.00 COMMENT 'Max spend per day in base currency',
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

## JPA Mapping Preview

These tables will be mapped to the following Spring Boot `@Entity` classes in the next sprint:

| SQL Table | Java Entity | Spring Data Repository |
|---|---|---|
| `users` | `User.java` | `UserRepository.java` |
| `virtual_cards` | `VirtualCard.java` | `VirtualCardRepository.java` |

The `user_id` foreign key will be expressed as a `@ManyToOne` / `@OneToMany` JPA relationship using `@JoinColumn(name = "user_id")`.

---

*Last updated: Sprint 1 — SCRUM-11 (Initial DB Schema — Users & Virtual Cards)*
