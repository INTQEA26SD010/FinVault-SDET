<p align="center">
  <img src="https://img.shields.io/badge/Spring%20Data%20JPA-6DB33F?style=for-the-badge&logo=spring&logoColor=white" />
  <img src="https://img.shields.io/badge/Hibernate-59666C?style=for-the-badge&logo=hibernate&logoColor=white" />
  <img src="https://img.shields.io/badge/Lombok-BC4521?style=for-the-badge" />
  <img src="https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white" />
</p>

# 💾 FinVault — JPA Entities & Repositories

> **Ticket:** SCRUM-13 | **Package:** `com.finvault.backend.entity` / `com.finvault.backend.repository`  
> **Framework:** Spring Data JPA + Hibernate ORM 7 | **Database:** MySQL 8.0 (`finvault_db`)

---

## 📑 Table of Contents

| # | Section | Description |
|:-:|---------|-------------|
| 1 | [What is JPA & ORM? — Theory](#-what-is-jpa--orm--theory) | Foundational concepts of object-relational mapping |
| 2 | [How Hibernate Works Under the Hood](#-how-hibernate-works-under-the-hood) | The magic behind automatic SQL generation |
| 3 | [Entity: `User`](#-entity-user) | Mapping, annotations, and design choices |
| 4 | [Entity: `VirtualCard`](#-entity-virtualcard) | Mapping, annotations, and design choices |
| 5 | [Entity: `Transaction`](#-entity-transaction) | Mapping, approval logic, and status enum |
| 6 | [The `@OneToMany` / `@ManyToOne` Relationship](#-the-onetomany--manytoone-relationship) | Understanding JPA relationships |
| 7 | [Repositories — Zero-Boilerplate Data Access](#-repositories--zero-boilerplate-data-access) | Spring Data JPA repository interfaces |
| 8 | [Derived Query Methods — SQL Without Writing SQL](#-derived-query-methods--sql-without-writing-sql) | How Spring generates queries from method names |
| 9 | [Lombok — Eliminating Java Boilerplate](#-lombok--eliminating-java-boilerplate) | Understanding `@Data`, `@NoArgsConstructor`, etc. |
| 10 | [Package Structure](#-package-structure) | File organization overview |
| 11 | [Common JPA Pitfalls & How FinVault Avoids Them](#-common-jpa-pitfalls--how-finvault-avoids-them) | Anti-patterns and best practices |
| 12 | [Glossary](#-glossary) | Key JPA/ORM terms |

---

## 📖 What is JPA & ORM? — Theory

### The Problem: Object-Relational Mismatch

Java is **object-oriented** — it thinks in terms of *objects* with fields and methods.  
MySQL is **relational** — it thinks in terms of *tables* with rows and columns.

These two worlds don't naturally fit together:

```
         JAVA WORLD                              DATABASE WORLD
  ┌─────────────────────┐                 ┌──────────────────────┐
  │  class User {       │                 │  CREATE TABLE users ( │
  │    Long id;         │   ← HOW DO     │    id BIGINT,         │
  │    String username; │   WE BRIDGE    │    username VARCHAR,   │
  │    String email;    │   THIS GAP? →  │    email VARCHAR,      │
  │    List<Card> cards;│                 │    -- no "list" here! │
  │  }                  │                 │  );                    │
  └─────────────────────┘                 └──────────────────────┘
```

### The Solution: ORM (Object-Relational Mapping)

**ORM** bridges this gap automatically:

```
  Java Object                  ORM (Hibernate)                  SQL Table
  ┌──────────┐                 ┌──────────────┐                 ┌──────────┐
  │  User    │  ──persist()──► │  Generates   │  ──executes──► │  users   │
  │  object  │                 │  INSERT INTO │                 │  table   │
  │          │  ◄──find()────  │  users ...   │  ◄──returns──  │          │
  └──────────┘                 └──────────────┘                 └──────────┘
```

### JPA vs Hibernate — What's the Difference?

| Concept | What It Is | Analogy |
|---------|-----------|---------|
| **JPA** (Java Persistence API) | A **specification** — defines the rules and annotations (`@Entity`, `@Id`, etc.) | Like a blueprint/contract |
| **Hibernate** | An **implementation** — the actual library that does the work | Like a builder who follows the blueprint |
| **Spring Data JPA** | A **convenience layer** on top of JPA — adds `JpaRepository` for zero-boilerplate CRUD | Like a project manager who delegates tasks to the builder |

> 💡 **In FinVault:** We write JPA annotations (`@Entity`, `@Table`, `@Column`), Hibernate does the SQL generation, and Spring Data JPA provides the `JpaRepository` interface.

---

## ⚙️ How Hibernate Works Under the Hood

When FinVault's Spring Boot application starts, here's what happens:

```
 🚀 Application Startup
    │
    ├── 1. Spring Boot scans for @Entity classes
    │       Found: User.java, VirtualCard.java
    │
    ├── 2. Hibernate reads JPA annotations on each entity
    │       @Table(name="users") → maps to MySQL `users` table
    │       @Column(name="email") → maps to `email` column
    │
    ├── 3. ddl-auto=update (from application.properties)
    │       Hibernate compares entities to actual DB tables
    │       Creates missing tables, adds missing columns
    │       ⚠️ Never drops existing columns (safe for development)
    │
    ├── 4. Spring Data scans for JpaRepository interfaces
    │       Found: UserRepository, VirtualCardRepository
    │       Creates proxy implementations automatically (no code needed!)
    │
    └── 5. Application is ready — all repositories are injectable via @Autowired
```

### `ddl-auto` Values Explained

| Value | Behavior | When to Use |
|:-----:|----------|:-----------:|
| `update` ⭐ | Creates/modifies tables to match entities; never drops | **Development** (FinVault uses this) |
| `create` | Drops and recreates all tables on every startup | Testing only |
| `create-drop` | Like `create`, but also drops tables on shutdown | Unit tests |
| `validate` | Only verifies entities match schema; throws error if mismatch | **Production** |
| `none` | Hibernate doesn't touch the schema at all | When using Flyway/Liquibase migrations |

---

## 👤 Entity: `User`

> **File:** `src/main/java/com/finvault/backend/entity/User.java`  
> **Maps to:** MySQL `users` table

### Visual Field Mapping

```
 User.java (@Entity)                              users (MySQL table)
 ═══════════════════════════════                   ═══════════════════════════
 @Id @GeneratedValue(IDENTITY)
 Long id;                          ────────────►   id BIGINT PK AUTO_INCREMENT

 @Column(unique=true, length=50)
 String username;                  ────────────►   username VARCHAR(50) UNIQUE NOT NULL

 @Column(unique=true, length=100)
 String email;                     ────────────►   email VARCHAR(100) UNIQUE NOT NULL

 @Column(name="password_hash")
 String passwordHash;              ────────────►   password_hash VARCHAR(60) NOT NULL

 LocalDateTime createdAt;          ────────────►   created_at DATETIME NOT NULL

 @OneToMany(mappedBy="user")
 List<VirtualCard> virtualCards;   ────────────►   (no column — inverse side of relationship)
```

### Actual Code with Annotations Explained

```java
@Entity                                       // ① Marks this class as a JPA entity
@Table(name = "users")                        // ② Explicitly names the SQL table
@Data                                         // ③ Lombok: generates getters, setters, equals, hashCode, toString
@NoArgsConstructor                            // ④ Lombok: generates no-arg constructor (required by JPA)
@AllArgsConstructor                           // ⑤ Lombok: generates all-args constructor (convenience)
public class User {

    @Id                                       // ⑥ Marks this field as the primary key
    @GeneratedValue(strategy = IDENTITY)      // ⑦ MySQL AUTO_INCREMENT generates the ID
    private Long id;

    @Column(name = "username",                // ⑧ Maps to `username` column
            nullable = false,                 //    NOT NULL constraint
            unique = true,                    //    UNIQUE constraint
            length = 50)                      //    VARCHAR(50)
    private String username;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 60)
    private String passwordHash;              // ⑨ BCrypt hash — NEVER plaintext

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;          // ⑩ updatable=false prevents overwriting

    @ToString.Exclude                         // ⑪ Prevents infinite recursion in toString()
    @OneToMany(mappedBy = "user",             // ⑫ Inverse side — "user" matches VirtualCard.user field
               cascade = CascadeType.ALL,     // ⑬ Persist/delete cards when user is persisted/deleted
               orphanRemoval = true)          // ⑭ Remove card from DB when removed from this list
    private List<VirtualCard> virtualCards = new ArrayList<>();

    @PrePersist                               // ⑮ JPA lifecycle callback — runs before INSERT
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
```

### Key Design Choices Explained

| # | Annotation / Pattern | Purpose | Why It Matters |
|:-:|---------------------|---------|----------------|
| ② | `@Table(name="users")` | Explicitly names the table | Avoids relying on Hibernate's naming strategy defaults — predictable mapping |
| ⑦ | `GenerationType.IDENTITY` | Uses MySQL's `AUTO_INCREMENT` | Compatible with Hibernate 6/7 and MySQL; no sequence tables needed |
| ⑨ | `passwordHash` field | Stores BCrypt output only | Raw passwords are **NEVER** persisted — Spring Security's `BCryptPasswordEncoder` produces a 60-char hash |
| ⑪ | `@ToString.Exclude` | Excludes `virtualCards` from `toString()` | Prevents `StackOverflowError` from bidirectional relationship |
| ⑬ | `cascade = ALL` | Cascades all JPA operations | `save(user)` also saves all their cards; `delete(user)` also deletes all their cards |
| ⑭ | `orphanRemoval = true` | Auto-deletes orphaned cards | If you remove a card from `user.getVirtualCards()`, Hibernate deletes it from the DB |
| ⑮ | `@PrePersist` | Sets `createdAt` before INSERT | Provides consistent timestamps regardless of DB timezone configuration |

---

## 💳 Entity: `VirtualCard`

> **File:** `src/main/java/com/finvault/backend/entity/VirtualCard.java`  
> **Maps to:** MySQL `virtual_cards` table

### Visual Field Mapping

```
 VirtualCard.java (@Entity)                        virtual_cards (MySQL table)
 ══════════════════════════════════                 ════════════════════════════════
 @Id @GeneratedValue(IDENTITY)
 Long id;                          ────────────►   id BIGINT PK AUTO_INCREMENT

 @ManyToOne(fetch=LAZY)
 @JoinColumn(name="user_id")
 User user;                        ────────────►   user_id BIGINT FK → users.id

 String cardNumber;                ────────────►   card_number CHAR(16) UNIQUE NOT NULL

 LocalDate expiryDate;             ────────────►   expiry_date DATE NOT NULL

 String cvv;                       ────────────►   cvv CHAR(3) NOT NULL

 BigDecimal dailyLimit;            ────────────►   daily_limit DECIMAL(10,2) DEFAULT 0.00

 @Enumerated(STRING)
 CardStatus status;                ────────────►   status ENUM('ACTIVE','FROZEN',...) DEFAULT 'ACTIVE'

 LocalDateTime createdAt;          ────────────►   created_at DATETIME NOT NULL
```

### Key Design Choices Explained

| Annotation / Type | Purpose | Why It Matters |
|:-----------------:|---------|----------------|
| `@ManyToOne(fetch = LAZY)` | Many cards belong to one user; LAZY = don't load user unless accessed | Avoids unnecessary `JOIN` on every card query — major performance win |
| `@JoinColumn(name = "user_id")` | Declares the FK column name explicitly | Matches the SQL DDL's `user_id` column; self-documenting |
| `BigDecimal` for `dailyLimit` | Exact decimal arithmetic | `float`/`double` cause rounding errors: `0.1 + 0.2 = 0.30000000000000004` — unacceptable for money |
| `@Enumerated(EnumType.STRING)` | Stores `"ACTIVE"` not `0` in the DB | If you reorder enum values in Java, DB data doesn't break |
| `CardStatus` inner enum | Mirrors the MySQL ENUM constraint | Type-safe in Java — compiler prevents invalid states |
| `@PrePersist` | Auto-sets `createdAt` | Consistent timestamps without relying on DB-specific defaults |

### Why `BigDecimal` Instead of `double` for Money?

```java
// ❌ double — DANGEROUS for financial calculations
double price = 0.1 + 0.2;
System.out.println(price);       // 0.30000000000000004 — WRONG!

// ✅ BigDecimal — EXACT for financial calculations
BigDecimal price = new BigDecimal("0.10").add(new BigDecimal("0.20"));
System.out.println(price);       // 0.30 — CORRECT!
```

> 🚨 **Rule for financial software:** _Always_ use `BigDecimal` for monetary values. The JPA `@Column(precision=10, scale=2)` maps to `DECIMAL(10,2)` in MySQL, maintaining precision end-to-end.

### Why `@Enumerated(STRING)` Instead of `ORDINAL`?

```java
// ❌ ORDINAL (default) — stores the enum's position number
// ACTIVE=0, FROZEN=1, EXPIRED=2, CANCELLED=3
// Problem: If you add a new status between ACTIVE and FROZEN,
//          ALL existing FROZEN rows become the new status!

// ✅ STRING — stores the enum's name as text
// ACTIVE="ACTIVE", FROZEN="FROZEN"
// Safe: Reordering or adding new values doesn't affect existing data
```

---

## � Entity: `Transaction`

> **File:** `src/main/java/com/finvault/backend/entity/Transaction.java`  
> **Maps to:** MySQL `transactions` table

### Visual Field Mapping

```
 Transaction.java (@Entity)                          transactions (MySQL table)
 ═══════════════════════════════     ══════════════════════════════════
 @Id @GeneratedValue(IDENTITY)
 Long id;                          ──────────►   id BIGINT PK AUTO_INCREMENT

 @ManyToOne(fetch=LAZY)
 @JoinColumn(name="virtual_card_id")
 VirtualCard virtualCard;          ──────────►   virtual_card_id BIGINT FK

 BigDecimal amount;                ──────────►   amount DECIMAL(10,2) NOT NULL

 String merchantName;              ──────────►   merchant_name VARCHAR(100) NOT NULL

 LocalDateTime timestamp;          ──────────►   timestamp DATETIME NOT NULL

 @Enumerated(STRING)
 TransactionStatus status;         ──────────►   status ENUM('SUCCESS','DECLINED')
```

### Actual Code with Annotations Explained

```java
@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ToString.Exclude                         // ① Prevents circular toString loop
    @ManyToOne(fetch = FetchType.LAZY,        // ② LAZY - don't load card on every tx query
               optional = false)
    @JoinColumn(name = "virtual_card_id",     // ③ FK column name in transactions table
                nullable = false)
    private VirtualCard virtualCard;

    @Column(name = "amount", nullable = false,
            precision = 10, scale = 2)        // ④ DECIMAL(10,2) - exact money
    private BigDecimal amount;

    @Column(name = "merchant_name",
            nullable = false, length = 100)
    private String merchantName;

    @Column(name = "timestamp",
            nullable = false, updatable = false)
    private LocalDateTime timestamp;

    @Enumerated(EnumType.STRING)              // ⑤ Stores "SUCCESS", not 0
    @Column(name = "status", nullable = false, length = 10)
    private TransactionStatus status;

    @PrePersist                               // ⑥ Auto-sets timestamp before INSERT
    protected void onCreate() {
        this.timestamp = LocalDateTime.now();
    }

    public enum TransactionStatus {
        SUCCESS,    // Amount within limit; balance updated
        DECLINED    // Amount would exceed limit; balance unchanged
    }
}
```

### Key Design Choices

| # | Pattern | Purpose | Why It Matters |
|:-:|---------|---------|----------------|
| ① | `@ToString.Exclude` | Excludes `virtualCard` from `toString()` | Prevents `StackOverflowError` from bidirectional loop |
| ② | `@ManyToOne(fetch = LAZY)` | Don't load VirtualCard on every transaction query | N+1 prevention; we only need `card.getId()` in `toResponseDto()` |
| ④ | `BigDecimal` amount | Exact decimal for money | `float`/`double` accumulate rounding errors |
| ⑤ | `@Enumerated(STRING)` | Store `"SUCCESS"` not `0` | Enum reordering in Java won't corrupt existing DB rows |
| ⑥ | `@PrePersist` timestamp | Auto-set before INSERT | Consistent timestamps, no reliance on DB-specific defaults |

> ⚠️ **Critical `@Transactional` note:** `getTransactionsByCardId()` in `TransactionService` is annotated `@Transactional(readOnly = true)`. Without this, accessing `tx.getVirtualCard().getId()` inside `toResponseDto()` throws `LazyInitializationException` because `spring.jpa.open-in-view=false` closes the Hibernate session before the service returns.

---

## �🔗 The `@OneToMany` / `@ManyToOne` Relationship

### How It Works in FinVault

```
              OWNING SIDE                              INVERSE SIDE
         (has the FK column)                     (uses mappedBy)
  ┌──────────────────────────────┐       ┌──────────────────────────────┐
  │      VirtualCard.java        │       │         User.java            │
  │                              │       │                              │
  │  @ManyToOne(fetch = LAZY)    │       │  @OneToMany(                 │
  │  @JoinColumn(name="user_id") │       │    mappedBy = "user",        │
  │  private User user;          │◄──────│    cascade = ALL,            │
  │                              │       │    orphanRemoval = true       │
  │  // This side OWNS the FK   │       │  )                           │
  │  // user_id column is HERE  │       │  private List<VirtualCard>   │
  └──────────────────────────────┘       │    virtualCards;             │
                                          │                              │
                                          │  // This side is INVERSE    │
                                          │  // No FK column here       │
                                          └──────────────────────────────┘
```

### Key Concepts

| Concept | Meaning | In FinVault |
|---------|---------|-------------|
| **Owning Side** | The entity that contains the `@JoinColumn` (FK column) | `VirtualCard` owns `user_id` |
| **Inverse Side** | The entity that uses `mappedBy` to reference the owning field | `User` has `mappedBy = "user"` |
| **LAZY Loading** | Related entity is NOT loaded until you call `.getUser()` | `card.getUser()` triggers a SELECT |
| **EAGER Loading** | Related entity is loaded immediately via JOIN | Not used here — too expensive |
| **Cascade** | Operations on parent automatically apply to children | Save/delete user → also save/delete their cards |
| **orphanRemoval** | Removing a child from the parent's list deletes it from DB | `user.getVirtualCards().remove(card)` → DELETE from virtual_cards |

---

## 🔍 Repositories — Zero-Boilerplate Data Access

### What is the Repository Pattern?

The **Repository pattern** provides a clean abstraction over data access. Instead of writing raw SQL, you extend an interface and Spring Data JPA provides the implementation automatically at runtime.

```
         YOUR CODE                   SPRING GENERATES AT RUNTIME
  ┌──────────────────────┐          ┌───────────────────────────────────┐
  │  public interface     │          │  class UserRepositoryImpl         │
  │  UserRepository       │  ─────► │  implements UserRepository {      │
  │  extends              │  (auto) │    public User save(User u) {     │
  │  JpaRepository        │          │      em.persist(u); // Hibernate │
  │  <User, Long> { }    │          │    }                              │
  └──────────────────────┘          │    public Optional<User>          │
                                     │    findById(Long id) {           │
                                     │      return em.find(User, id);   │
                                     │    }                              │
                                     │    // + 15 more CRUD methods!    │
                                     │  }                                │
                                     └───────────────────────────────────┘
```

### `UserRepository`

```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);         // Login lookup
    Optional<User> findByUsername(String username);    // Duplicate check
    boolean existsByEmail(String email);              // Registration guard
}
```

### `VirtualCardRepository`

```java
@Repository
public interface VirtualCardRepository extends JpaRepository<VirtualCard, Long> {

    List<VirtualCard> findByUserId(Long userId);                        // Dashboard cards
    Optional<VirtualCard> findByCardNumber(String cardNumber);          // Transaction lookup
    List<VirtualCard> findByUserIdAndStatus(Long userId, CardStatus s); // Filtered view
}
```

### Inherited Methods from `JpaRepository`

By extending `JpaRepository<User, Long>`, you get **all these methods for free**:

| Method | SQL Equivalent | Returns |
|--------|---------------|---------|
| `save(entity)` | `INSERT` or `UPDATE` | The saved entity with generated ID |
| `findById(id)` | `SELECT * WHERE id = ?` | `Optional<Entity>` |
| `findAll()` | `SELECT *` | `List<Entity>` |
| `deleteById(id)` | `DELETE WHERE id = ?` | `void` |
| `count()` | `SELECT COUNT(*)` | `long` |
| `existsById(id)` | `SELECT COUNT(*) > 0 WHERE id = ?` | `boolean` |

---

## 🧙 Derived Query Methods — SQL Without Writing SQL

Spring Data JPA can **generate SQL automatically** from method names. It parses the method name and constructs the query:

### How Method Name Parsing Works

```
findByEmailAndStatus(String email, CardStatus status)
  │   │     │    │
  │   │     │    └── AND status = ?     ← second condition
  │   │     └─────── email = ?          ← first condition
  │   └───────────── WHERE clause       ← "By" starts the WHERE
  └───────────────── SELECT operation   ← "find" = SELECT
```

### FinVault's Custom Queries — Auto-Generated SQL

| Repository | Method | Generated SQL |
|:----------:|--------|:-------------:|
| `UserRepository` | `findByEmail(String email)` | `SELECT * FROM users WHERE email = ?` |
| `UserRepository` | `findByUsername(String username)` | `SELECT * FROM users WHERE username = ?` |
| `UserRepository` | `existsByEmail(String email)` | `SELECT COUNT(*) > 0 FROM users WHERE email = ?` |
| `VirtualCardRepository` | `findByUserId(Long userId)` | `SELECT * FROM virtual_cards WHERE user_id = ?` |
| `VirtualCardRepository` | `findByCardNumber(String cardNumber)` | `SELECT * FROM virtual_cards WHERE card_number = ?` |
| `VirtualCardRepository` | `findByUserIdAndStatus(Long, CardStatus)` | `SELECT * FROM virtual_cards WHERE user_id = ? AND status = ?` |

### Keyword Reference (Most Common)

| Keyword | Example | Generated SQL Clause |
|---------|---------|---------------------|
| `And` | `findByNameAndAge` | `WHERE name = ? AND age = ?` |
| `Or` | `findByNameOrAge` | `WHERE name = ? OR age = ?` |
| `Between` | `findByAgeBetween` | `WHERE age BETWEEN ? AND ?` |
| `LessThan` | `findByAgeLessThan` | `WHERE age < ?` |
| `GreaterThan` | `findByAgeGreaterThan` | `WHERE age > ?` |
| `Like` | `findByNameLike` | `WHERE name LIKE ?` |
| `OrderBy` | `findByNameOrderByAgeDesc` | `WHERE name = ? ORDER BY age DESC` |
| `Not` | `findByNameNot` | `WHERE name != ?` |
| `In` | `findByStatusIn` | `WHERE status IN (?, ?, ...)` |
| `IsNull` | `findByEmailIsNull` | `WHERE email IS NULL` |

---

## 🏭 Lombok — Eliminating Java Boilerplate

### What is Lombok?

**Project Lombok** is a Java library that uses annotation processing to generate repetitive code at compile time — getters, setters, constructors, `equals()`, `hashCode()`, `toString()`, etc.

### Annotations Used in FinVault

| Annotation | What It Generates | Without Lombok (LOC) |
|:----------:|-------------------|:--------------------:|
| `@Data` | `@Getter` + `@Setter` + `@ToString` + `@EqualsAndHashCode` + `@RequiredArgsConstructor` | ~50 lines per entity |
| `@NoArgsConstructor` | `public User() {}` | 1 line (but JPA requires it!) |
| `@AllArgsConstructor` | `public User(Long id, String username, ...)` | 5+ lines |
| `@RequiredArgsConstructor` | Constructor for `final` fields only | 5+ lines |
| `@ToString.Exclude` | Excludes a field from `toString()` output | Prevents circular reference crashes |

### Before & After Lombok

```java
// ❌ WITHOUT LOMBOK — 60+ lines of boilerplate
public class User {
    private Long id;
    private String username;
    private String email;

    public User() {}
    public User(Long id, String username, String email) {
        this.id = id; this.username = username; this.email = email;
    }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    @Override public boolean equals(Object o) { ... }
    @Override public int hashCode() { ... }
    @Override public String toString() { ... }
}

// ✅ WITH LOMBOK — 5 lines, same functionality!
@Data @NoArgsConstructor @AllArgsConstructor
public class User {
    private Long id;
    private String username;
    private String email;
}
```

---

## 📂 Package Structure

```
com.finvault.backend
│
├── 📂 entity/                              ← Domain Model Layer
│   ├── User.java                           ← @Entity → maps to `users` table
│   │   └── Fields: id, username, email, passwordHash, createdAt, virtualCards
│   │
│   ├── VirtualCard.java                    ← @Entity → maps to `virtual_cards` table
│   │   ├── Fields: id, user, cardNumber, expiryDate, cvv, dailyLimit, balance, status, createdAt
│   │   └── CardStatus enum: ACTIVE, FROZEN, EXPIRED, CANCELLED
│   │
│   └── Transaction.java                    ← @Entity → maps to `transactions` table
│       ├── Fields: id, virtualCard, amount, merchantName, timestamp, status
│       └── TransactionStatus enum: SUCCESS, DECLINED
│
└── 📂 repository/                          ← Data Access Layer
    ├── UserRepository.java                 ← JpaRepository<User, Long>
    │   └── Custom: findByEmail, findByUsername, existsByEmail
    │
    ├── VirtualCardRepository.java          ← JpaRepository<VirtualCard, Long>
    │   └── Custom: findByUserId, findByCardNumber, findByUserIdAndStatus
    │
    └── TransactionRepository.java          ← JpaRepository<Transaction, Long>
        └── Custom: findByVirtualCardIdOrderByTimestampDesc(Long cardId)
```

---

## ⚠️ Common JPA Pitfalls & How FinVault Avoids Them

| # | Pitfall | Impact | FinVault's Solution |
|:-:|---------|--------|:-------------------:|
| 1 | **Returning @Entity from controller** | Exposes password hashes, CVVs to API consumers | Uses DTOs (`UserRegistrationDto`, `VirtualCardResponseDto`, `TransactionResponseDto`) |
| 2 | **Bidirectional `toString()` loop** | `User.toString()` calls `Card.toString()` which calls `User.toString()` → `StackOverflowError` | `@ToString.Exclude` on relationship fields in all entities |
| 3 | **EAGER loading by default** | `@OneToMany` loads ALL cards on EVERY user query — N+1 problem | `@ManyToOne(fetch = LAZY)` on VirtualCard and Transaction |
| 4 | **Using `float`/`double` for money** | Rounding errors in financial calculations | `BigDecimal` + `DECIMAL(10,2)` |
| 5 | **Using `@Enumerated(ORDINAL)`** | Adding/reordering enum values corrupts existing DB data | `@Enumerated(EnumType.STRING)` — stores name, not position |
| 6 | **Missing `@PrePersist`** | `createdAt` is null if application doesn't set it | `@PrePersist protected void onCreate()` auto-sets before INSERT |
| 7 | **No orphan removal** | Removing card from user's list leaves it orphaned in DB | `orphanRemoval = true` on `@OneToMany` |
| 8 | **Missing `@Transactional` on read services** | `LazyInitializationException` when accessing LAZY proxy after Hibernate session closes | `@Transactional(readOnly = true)` on `getTransactionsByCardId()` in `TransactionService` |

---

## 📚 Glossary

| Term | Definition |
|------|-----------|
| **JPA** | Java Persistence API — the standard specification for ORM in Java |
| **ORM** | Object-Relational Mapping — bridging Java objects and SQL tables |
| **Hibernate** | The most popular JPA implementation — generates SQL from annotations |
| **Entity** | A Java class annotated with `@Entity` that maps to a database table |
| **Repository** | An interface that provides data access methods (CRUD) without writing SQL |
| **LAZY Loading** | Related entities are loaded only when explicitly accessed (on-demand) |
| **EAGER Loading** | Related entities are loaded immediately with the parent (via JOIN) |
| **Cascade** | Propagating JPA operations (persist, delete, etc.) from parent to child entities |
| **orphanRemoval** | Automatically deleting child entities that are removed from the parent's collection |
| **@PrePersist** | A JPA lifecycle callback that runs just before the `INSERT` SQL statement |
| **Derived Query** | A Spring Data JPA method whose SQL is generated from its method name |
| **ddl-auto** | Hibernate property controlling how schema changes are handled on startup |
| **Lombok** | A Java library that generates boilerplate code (getters, setters, etc.) at compile time |
| **DTO** | Data Transfer Object — a plain class used to safely transfer data between layers |
| **Surrogate Key** | An artificial primary key (like auto-increment `id`) with no business meaning |

---

<p align="center">
  <b>💾 FinVault JPA Entities & Repositories Document</b><br>
  <sub>Sprint 1 — SCRUM-13 (JPA Entities & Repositories) | Sprint 2 — SCRUM-17 (Transaction Entity)</sub><br>
  <sub>Part of the <a href="ARCHITECTURE.md">FinVault Documentation Suite</a></sub>
</p>
