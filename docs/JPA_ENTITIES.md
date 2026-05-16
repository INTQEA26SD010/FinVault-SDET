<p align="center">
  <img src="https://img.shields.io/badge/Spring%20Data%20JPA-6DB33F?style=for-the-badge&logo=spring&logoColor=white" />
  <img src="https://img.shields.io/badge/Hibernate%207-59666C?style=for-the-badge&logo=hibernate&logoColor=white" />
  <img src="https://img.shields.io/badge/Lombok-BC4521?style=for-the-badge" />
  <img src="https://img.shields.io/badge/Entities-3-blue?style=for-the-badge" />
</p>

# 💾 FinVault — JPA Entities & Repositories

> **Package:** `com.finvault.backend.entity` / `com.finvault.backend.repository`  
> **Framework:** Spring Data JPA + Hibernate 7 | **Database:** MySQL 8.0 (`finvault_db`)

---

## 📑 Table of Contents

| # | Section |
|:-:|---------|
| 1 | [JPA & ORM Theory](#-jpa--orm-theory) |
| 2 | [Hibernate Startup Sequence](#-hibernate-startup-sequence) |
| 3 | [Entity: User](#-entity-user) |
| 4 | [Entity: VirtualCard](#-entity-virtualcard) |
| 5 | [Entity: Transaction](#-entity-transaction) |
| 6 | [Relationships (@OneToMany / @ManyToOne)](#-relationships-onetomany--manytoone) |
| 7 | [Repositories — Zero-Boilerplate Data Access](#-repositories--zero-boilerplate-data-access) |
| 8 | [Derived Query Methods](#-derived-query-methods) |
| 9 | [Lombok Annotations](#-lombok-annotations) |
| 10 | [Common JPA Pitfalls & Solutions](#-common-jpa-pitfalls--solutions) |

---

## 📖 JPA & ORM Theory

### The Object-Relational Mismatch Problem

```
     JAVA WORLD                              DATABASE WORLD
┌─────────────────────┐                 ┌──────────────────────┐
│  class User {       │                 │  CREATE TABLE users ( │
│    Long id;         │   HOW DO WE    │    id BIGINT,         │
│    String username; │   BRIDGE THIS  │    username VARCHAR,   │
│    List<Card> cards;│   GAP?    →    │    -- no "list" here! │
│  }                  │                 │  );                    │
└─────────────────────┘                 └──────────────────────┘
```

**ORM (Object-Relational Mapping)** bridges this gap automatically:

```
  Java Object  ──persist()──►  Hibernate  ──executes──►  SQL Table
  User object                  generates                  users row
               ◄──find()────              ◄──returns──
```

### JPA vs Hibernate vs Spring Data JPA

| Layer | Role | Analogy |
|-------|------|---------|
| **JPA** | A *specification* — defines annotations (`@Entity`, `@Id`) | Blueprint/contract |
| **Hibernate** | An *implementation* — generates and executes SQL | Builder who follows the blueprint |
| **Spring Data JPA** | A *convenience layer* — provides `JpaRepository` | Project manager who delegates |

### 🎓 Interview Summary

> "We annotate our classes with JPA annotations, Hibernate translates them to SQL, and Spring Data JPA gives us repository interfaces with zero-boilerplate CRUD operations."

---

## ⚙️ Hibernate Startup Sequence

```
🚀 Spring Boot starts
  │
  ├── 1. Scans for @Entity classes
  │     Found: User, VirtualCard, Transaction
  │
  ├── 2. Reads JPA annotations
  │     @Table(name="users") → maps to MySQL `users` table
  │     @Column(name="email") → maps to `email` column
  │
  ├── 3. ddl-auto=update
  │     Compares entities to actual DB tables
  │     Creates missing tables, adds missing columns
  │     ⚠️ Never drops existing columns (safe for dev)
  │
  ├── 4. Scans for JpaRepository interfaces
  │     Found: UserRepository, VirtualCardRepository, TransactionRepository
  │     Creates proxy implementations automatically
  │
  └── 5. Application ready — repositories injectable via @Autowired / @RequiredArgsConstructor
```

### `ddl-auto` Values

| Value | Behavior | Environment |
|:-----:|----------|:-----------:|
| `update` ⭐ | Creates/modifies tables; never drops | **Development** (FinVault) |
| `validate` | Verifies only; throws error on mismatch | **Production** |
| `create` | Drops + recreates on every startup | Testing |
| `none` | Hibernate doesn't touch schema | When using Flyway/Liquibase |

---

## 👤 Entity: User

> **File:** `entity/User.java` → **Table:** `users`

### Field Mapping

| Java Field | Annotation | SQL Column | Notes |
|-----------|-----------|-----------|-------|
| `Long id` | `@Id @GeneratedValue(IDENTITY)` | `id BIGINT PK AI` | MySQL AUTO_INCREMENT |
| `String username` | `@Column(unique=true, length=50)` | `username VARCHAR(50) UQ NN` | |
| `String email` | `@Column(unique=true, length=100)` | `email VARCHAR(100) UQ NN` | |
| `String passwordHash` | `@Column(name="password_hash", length=60)` | `password_hash VARCHAR(60) NN` | BCrypt hash only |
| `LocalDateTime createdAt` | `@Column(updatable=false)` | `created_at DATETIME NN` | Set by `@PrePersist` |
| `List<VirtualCard> virtualCards` | `@OneToMany(mappedBy="user", cascade=ALL, orphanRemoval=true)` | *(no column)* | Inverse side |

### Code

```java
@Entity
@Table(name = "users")
@Data @NoArgsConstructor @AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 60)
    private String passwordHash;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ToString.Exclude
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VirtualCard> virtualCards = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
```

### 🎓 Key Design Decisions

| Pattern | Why |
|---------|-----|
| `GenerationType.IDENTITY` | Uses MySQL's `AUTO_INCREMENT` — no sequence tables needed |
| `passwordHash` (not `password`) | Makes it explicit that raw passwords are NEVER stored |
| `@ToString.Exclude` on collection | Prevents `StackOverflowError` from bidirectional recursion |
| `cascade = ALL` | Persist/delete cards when user is persisted/deleted |
| `orphanRemoval = true` | Removing a card from the list auto-deletes it from DB |
| `@PrePersist` | Consistent timestamps regardless of DB timezone config |

---

## 💳 Entity: VirtualCard

> **File:** `entity/VirtualCard.java` → **Table:** `virtual_cards`

### Field Mapping

| Java Field | Annotation | SQL Column | Notes |
|-----------|-----------|-----------|-------|
| `Long id` | `@Id @GeneratedValue(IDENTITY)` | `id BIGINT PK AI` | |
| `User user` | `@ManyToOne(LAZY) @JoinColumn("user_id")` | `user_id BIGINT FK NN` | Owner side |
| `String cardNumber` | `@Column(unique=true, length=16)` | `card_number CHAR(16) UQ NN` | Random 16 digits |
| `LocalDate expiryDate` | `@Column(name="expiry_date")` | `expiry_date DATE NN` | now + 3 years |
| `String cvv` | `@Column(length=3)` | `cvv CHAR(3) NN` | Random 3 digits |
| `BigDecimal dailyLimit` | `@Column(precision=10, scale=2)` | `daily_limit DECIMAL(10,2) NN` | Max spend/day |
| `BigDecimal balance` | `@Column(precision=10, scale=2)` | `balance DECIMAL(10,2) NN` | Running total spent |
| `CardStatus status` | `@Enumerated(STRING)` | `status ENUM(...) NN` | ACTIVE/FROZEN/EXPIRED/CANCELLED |
| `String vendorName` | `@Column(columnDefinition="VARCHAR(100) NOT NULL DEFAULT ''")` | `vendor_name VARCHAR(100) NN` | Purpose label |
| `List<Transaction> transactions` | `@OneToMany(mappedBy="virtualCard", cascade=ALL, orphanRemoval=true)` | *(no column)* | |
| `LocalDateTime createdAt` | `@Column(updatable=false)` | `created_at DATETIME NN` | |

### CardStatus Enum

```java
public enum CardStatus {
    ACTIVE,      // Card is operational — transactions are processed
    FROZEN,      // Card is suspended — all transactions are declined
    EXPIRED,     // Card has passed its expiry date
    CANCELLED    // Card has been permanently deactivated
}
```

### 🎓 Key Design Decisions

| Pattern | Why |
|---------|-----|
| `@Enumerated(EnumType.STRING)` | Stores "ACTIVE" not 0 — safe for enum reordering; human-readable in SQL |
| `BigDecimal` for money | IEEE 754 double has precision loss (0.1+0.2≠0.3); BigDecimal is exact |
| `FetchType.LAZY` on `@ManyToOne` | Avoids N+1: loading a card doesn't force-load the entire User object |
| `vendorName` DEFAULT '' | Backward-compatible schema migration — existing rows survive Hibernate's `ddl-auto=update` |
| `CASCADE ALL` on transactions | Deleting a card cascades to its transactions — prevents FK violations |
| `balance` field | Performance optimization — avoids re-summing all transactions on every request |

---

## 📊 Entity: Transaction

> **File:** `entity/Transaction.java` → **Table:** `transactions`

### Field Mapping

| Java Field | Annotation | SQL Column | Notes |
|-----------|-----------|-----------|-------|
| `Long id` | `@Id @GeneratedValue(IDENTITY)` | `id BIGINT PK AI` | |
| `VirtualCard virtualCard` | `@ManyToOne(LAZY) @JoinColumn("virtual_card_id")` | `virtual_card_id BIGINT FK NN` | Owner side |
| `BigDecimal amount` | `@Column(precision=10, scale=2)` | `amount DECIMAL(10,2) NN` | |
| `String merchantName` | `@Column(length=100)` | `merchant_name VARCHAR(100) NN` | |
| `LocalDateTime timestamp` | `@Column(updatable=false)` | `timestamp DATETIME NN` | Set by `@PrePersist` |
| `TransactionStatus status` | `@Enumerated(STRING)` | `status ENUM(...) NN` | SUCCESS/DECLINED |

### TransactionStatus Enum

```java
public enum TransactionStatus {
    SUCCESS,    // Amount was within limit; balance updated
    DECLINED    // Amount exceeds daily limit; balance unchanged
}
```

### 🎓 Why Store DECLINED Transactions?

> Every attempt — approved or declined — is persisted for **audit purposes**. In financial systems, the audit trail must be complete. Knowing that a card was repeatedly declined helps detect potential fraud or misconfigured limits.

---

## 🔗 Relationships (@OneToMany / @ManyToOne)

```
  User (1) ───────── (M) VirtualCard (1) ───────── (M) Transaction
       │ @OneToMany        │ @ManyToOne        │ @OneToMany         │ @ManyToOne
       │ mappedBy="user"   │ @JoinColumn       │ mappedBy=          │ @JoinColumn
       │ cascade=ALL       │ "user_id"         │ "virtualCard"      │ "virtual_card_id"
       │ orphanRemoval     │ LAZY              │ cascade=ALL        │ LAZY
```

### Relationship Rules

| Rule | Implementation | Why |
|------|---------------|-----|
| Owner side has `@JoinColumn` | VirtualCard owns the User FK; Transaction owns the Card FK | JPA requires one side to "own" the FK column |
| Inverse side has `mappedBy` | User's `virtualCards` list; VirtualCard's `transactions` list | Tells Hibernate "I don't own a column — look at the other entity" |
| `LAZY` on `@ManyToOne` | Loading a Transaction doesn't force-load the VirtualCard | Prevents N+1 query explosion |
| `@ToString.Exclude` on collections | Both User and VirtualCard exclude their child lists | Prevents infinite recursion in `toString()` |

---

## 🔍 Repositories — Zero-Boilerplate Data Access

### UserRepository

```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    boolean existsByEmail(String email);
}
```

### VirtualCardRepository

```java
@Repository
public interface VirtualCardRepository extends JpaRepository<VirtualCard, Long> {
    List<VirtualCard> findByUserId(Long userId);
    Optional<VirtualCard> findByCardNumber(String cardNumber);
    List<VirtualCard> findByUserIdAndStatus(Long userId, CardStatus status);
}
```

### TransactionRepository

```java
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByVirtualCardIdOrderByTimestampDesc(Long virtualCardId);
}
```

### 🎓 What Does `JpaRepository<VirtualCard, Long>` Give You For Free?

| Method | Generated SQL |
|--------|--------------|
| `save(entity)` | INSERT or UPDATE (checks if ID exists) |
| `findById(id)` | `SELECT * FROM virtual_cards WHERE id = ?` |
| `findAll()` | `SELECT * FROM virtual_cards` |
| `deleteById(id)` | `DELETE FROM virtual_cards WHERE id = ?` |
| `existsById(id)` | `SELECT COUNT(*) > 0 FROM virtual_cards WHERE id = ?` |
| `count()` | `SELECT COUNT(*) FROM virtual_cards` |

---

## 🔧 Derived Query Methods

Spring Data parses method names and auto-generates SQL:

| Method Name | Generated Query |
|-------------|----------------|
| `findByEmail(String email)` | `WHERE email = ?` |
| `findByUserId(Long userId)` | `WHERE user_id = ?` |
| `findByUserIdAndStatus(Long userId, CardStatus status)` | `WHERE user_id = ? AND status = ?` |
| `findByVirtualCardIdOrderByTimestampDesc(Long id)` | `WHERE virtual_card_id = ? ORDER BY timestamp DESC` |
| `existsByEmail(String email)` | `SELECT COUNT(*) > 0 WHERE email = ?` |

### 🎓 How Does This Work? (Interview)

> Spring Data uses a **method name parser** at startup. It splits the name into parts: `findBy` (query type) + `Email` (property) → generates a JPQL query → Hibernate translates to SQL. No implementation class needed — Spring creates a **proxy** at runtime.

---

## 🧹 Lombok Annotations

| Annotation | Generates | Used On |
|-----------|-----------|---------|
| `@Data` | getters, setters, `equals()`, `hashCode()`, `toString()` | All entities + DTOs |
| `@NoArgsConstructor` | Zero-arg constructor (required by JPA) | All entities + DTOs |
| `@AllArgsConstructor` | Constructor with all fields | All entities + DTOs |
| `@RequiredArgsConstructor` | Constructor for `final` fields | Services + Controllers |
| `@ToString.Exclude` | Excludes field from `toString()` | Collection fields (prevents recursion) |
| `@Slf4j` | Creates `private static final Logger log` | TransactionService |

### 🎓 Why Lombok? (Interview)

> Lombok eliminates 60-80% of Java boilerplate. A typical entity with 6 fields would need ~80 lines of getters/setters/constructors/equals/hashCode. With `@Data`, it's 0 lines of boilerplate. The code is generated at compile time — no runtime overhead.

---

## ⚠️ Common JPA Pitfalls & Solutions

| Pitfall | Problem | FinVault's Solution |
|---------|---------|--------------------| 
| **N+1 Query** | Loading a list of cards also fires a query per card for the User | `FetchType.LAZY` on `@ManyToOne` — User loaded only when explicitly accessed |
| **Infinite recursion in toString** | `User.toString()` calls `cards.toString()` calls `user.toString()` → StackOverflow | `@ToString.Exclude` on all `@OneToMany` and `@ManyToOne` fields |
| **Exposing entities in API** | Serializing `User` to JSON exposes `passwordHash` | DTOs (`VirtualCardResponseDto`) — never return `@Entity` directly |
| **Float/Double for money** | `0.1 + 0.2 = 0.30000000000000004` | `BigDecimal` with `precision=10, scale=2` |
| **Enum ordinal stored as int** | Adding a new status between existing ones shifts all values | `@Enumerated(EnumType.STRING)` — stores the name, not the position |
| **Missing `@Transactional`** | Multi-step operations (toggle + save) can partially fail | `@Transactional` on service methods ensures atomicity |
| **Orphaned child rows** | Removing a card from `user.virtualCards` list leaves the DB row | `orphanRemoval = true` — Hibernate auto-deletes orphans |

---

<p align="center">
  <b>💾 FinVault JPA Entities & Repositories</b><br>
  <sub>3 entities | 3 repositories | Hibernate 7 | Lombok | Derived queries</sub><br>
  <sub>Part of the <a href="../README.md">FinVault Documentation Suite</a></sub>
</p>
