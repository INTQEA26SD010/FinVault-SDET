# FinVault — JPA Entities & Repositories

> **Ticket:** SCRUM-13 | **Package:** `com.finvault.backend.entity` / `com.finvault.backend.repository`  
> **Framework:** Spring Data JPA + Hibernate ORM 7 | **Database:** MySQL 8.0 (`finvault_db`)

---

## Overview

This document explains how the two core Java entity classes — `User` and `VirtualCard` — map to the relational schema defined in [DB_SCHEMA.md](DB_SCHEMA.md). These classes form the **domain model layer** of the FinVault backend. When the application starts, Hibernate reads the JPA annotations and automatically synchronises the database schema via `ddl-auto=update`, creating or altering the `users` and `virtual_cards` tables as needed.

---

## Entity: `User`

**File:** `src/main/java/com/finvault/backend/entity/User.java`  
**Table:** `users`

```
User.java  ←─────────────────────────  users (MySQL table)
─────────────────────────────────────────────────────────────
id            BIGINT PK AUTO_INCREMENT
username      VARCHAR(50)  UNIQUE NOT NULL
email         VARCHAR(100) UNIQUE NOT NULL
passwordHash  VARCHAR(60)  NOT NULL       ← BCrypt hash only
createdAt     DATETIME     NOT NULL       ← set by @PrePersist
virtualCards  (not a column — JPA @OneToMany relationship)
```

### Key Design Choices

| Annotation | Purpose |
|---|---|
| `@Entity` + `@Table(name="users")` | Maps the class to the `users` table explicitly (avoids relying on naming defaults) |
| `@Id` + `@GeneratedValue(IDENTITY)` | Delegates PK generation to MySQL's `AUTO_INCREMENT` — compatible with Hibernate 6/7 |
| `@Column(unique=true)` on email/username | Mirrors the `UNIQUE` constraints in the SQL DDL |
| `@OneToMany(cascade=ALL, orphanRemoval=true)` | One user → many cards; deleting a user cascades to all their cards |
| `@PrePersist` on `onCreate()` | Sets `createdAt` at the Java layer before `INSERT` — provides consistent timestamps regardless of DB timezone |
| Lombok `@Data` | Generates getters, setters, `equals()`, `hashCode()`, and `toString()` at compile time |

---

## Entity: `VirtualCard`

**File:** `src/main/java/com/finvault/backend/entity/VirtualCard.java`  
**Table:** `virtual_cards`

```
VirtualCard.java  ←───────────────────  virtual_cards (MySQL table)
─────────────────────────────────────────────────────────────────────
id          BIGINT PK AUTO_INCREMENT
user        → user_id BIGINT FK → users.id   ← @ManyToOne @JoinColumn
cardNumber  CHAR(16)   UNIQUE NOT NULL
expiryDate  DATE       NOT NULL
cvv         CHAR(3)    NOT NULL
dailyLimit  DECIMAL(10,2) DEFAULT 0.00       ← BigDecimal (no precision loss)
status      ENUM('ACTIVE','FROZEN',...)      ← @Enumerated(STRING)
createdAt   DATETIME   NOT NULL
```

### Key Design Choices

| Annotation / Type | Purpose |
|---|---|
| `@ManyToOne(fetch=LAZY)` | Many cards belong to one user; LAZY loading avoids a JOIN on every card fetch |
| `@JoinColumn(name="user_id")` | Declares the FK column name explicitly, matching the SQL DDL |
| `BigDecimal` for `dailyLimit` | Monetary precision — `float`/`double` introduce rounding errors that are unacceptable in financial software |
| `@Enumerated(EnumType.STRING)` | Stores the enum name (`"ACTIVE"`) not its ordinal (`0`) — safe if enum order changes in the future |
| `CardStatus` inner enum | Mirrors the MySQL ENUM constraint; type-safe at the Java layer |
| `@PrePersist` on `onCreate()` | Auto-sets `createdAt` before every insert |

---

## Repositories

**File:** `src/main/java/com/finvault/backend/repository/UserRepository.java`  
**File:** `src/main/java/com/finvault/backend/repository/VirtualCardRepository.java`

Both interfaces extend `JpaRepository<Entity, Long>`, which provides the full CRUD surface (`save`, `findById`, `findAll`, `delete`, etc.) with zero boilerplate. Additional **derived query methods** are declared using Spring Data's naming convention — Hibernate generates the SQL automatically:

| Method | Generated SQL |
|---|---|
| `findByEmail(String email)` | `SELECT * FROM users WHERE email = ?` |
| `findByUsername(String username)` | `SELECT * FROM users WHERE username = ?` |
| `existsByEmail(String email)` | `SELECT COUNT(*) > 0 FROM users WHERE email = ?` |
| `findByUserId(Long userId)` | `SELECT * FROM virtual_cards WHERE user_id = ?` |
| `findByCardNumber(String cardNumber)` | `SELECT * FROM virtual_cards WHERE card_number = ?` |
| `findByUserIdAndStatus(Long, CardStatus)` | `SELECT * FROM virtual_cards WHERE user_id = ? AND status = ?` |

---

## Package Structure

```
com.finvault.backend
├── entity/
│   ├── User.java             ← @Entity — maps to `users` table
│   └── VirtualCard.java      ← @Entity — maps to `virtual_cards` table
└── repository/
    ├── UserRepository.java         ← JpaRepository<User, Long>
    └── VirtualCardRepository.java  ← JpaRepository<VirtualCard, Long>
```

---

*Last updated: Sprint 1 — SCRUM-13 (JPA Entities & Repositories)*
