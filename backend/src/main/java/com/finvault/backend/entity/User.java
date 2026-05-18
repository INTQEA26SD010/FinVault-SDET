package com.finvault.backend.entity;

// ─────────────────────────────────────────────────────────────────────────────
// IMPORTS — Libraries we need for this class
// ─────────────────────────────────────────────────────────────────────────────

// Jakarta Persistence (JPA) annotations — these tell Spring how to map this
// Java class to a database table. Think of JPA as a bridge between Java objects
// and SQL tables — you write Java, JPA writes the SQL for you.
import jakarta.persistence.*;

// Lombok annotations — these auto-generate boilerplate code at compile time,
// so you don't have to manually write getters, setters, constructors, etc.
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// ─────────────────────────────────────────────────────────────────────────────
// USER ENTITY — This class represents the "users" table in our MySQL database.
// ─────────────────────────────────────────────────────────────────────────────
//
// WHAT IS AN ENTITY?
// An Entity is a Java class that maps directly to a database table.
// Each instance (object) of this class = one row in the "users" table.
// Each field in this class = one column in that table.
//
// WHAT DOES THIS CLASS DO?
// It stores information about a registered FinVault user — their username,
// email, hashed password, and the list of virtual cards they own.
//
// HOW IT FITS IN THE PROJECT:
// Frontend (Angular) → Controller → Service → Repository → THIS ENTITY → Database
//
// ─────────────────────────────────────────────────────────────────────────────

@Entity          // Tells JPA: "This class is a database table — manage it for me"
@Table(name = "users")  // Specifies the exact table name in MySQL (otherwise JPA would use class name)

// LOMBOK ANNOTATIONS (auto-generate code so we don't write it manually):
@Data            // Generates: getters, setters, toString(), equals(), hashCode() for ALL fields
@NoArgsConstructor   // Generates: an empty constructor → public User() {}   (JPA requires this)
@AllArgsConstructor  // Generates: a constructor with ALL fields as parameters
public class User {

    // ─── PRIMARY KEY ─────────────────────────────────────────────────────────
    // Every database table needs a unique identifier for each row.
    // This is like a roll number — no two users can have the same id.
    @Id  // Marks this field as the PRIMARY KEY of the table
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // ↑ AUTO_INCREMENT in MySQL — the database automatically assigns 1, 2, 3...
    // You never set this manually; MySQL handles it when you INSERT a new row.
    private Long id;

    // ─── USERNAME ────────────────────────────────────────────────────────────
    // The display name chosen by the user during registration (e.g., "johndoe")
    @Column(name = "username",    // Maps to the "username" column in the table
            nullable = false,      // NOT NULL — this field MUST have a value
            unique = true,         // UNIQUE — no two users can have the same username
            length = 50)           // VARCHAR(50) — max 50 characters in the database
    private String username;

    // ─── EMAIL ───────────────────────────────────────────────────────────────
    // The user's email address — used for login (acts as their identity)
    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    // ─── PASSWORD HASH ───────────────────────────────────────────────────────
    // IMPORTANT SECURITY CONCEPT:
    // We NEVER store the actual password (e.g., "MyPassword123") in the database.
    // Instead, we store a "hash" — a one-way encrypted version of the password.
    //
    // Example:
    //   Password: "hello"  →  Hash: "$2a$10$N9qo8uLOickgx2ZMRZoMye..."
    //
    // Why? If a hacker steals the database, they can't read anyone's password.
    // BCrypt produces a 60-character hash string, hence length = 60.
    @Column(name = "password_hash", nullable = false, length = 60)
    private String passwordHash;

    // ─── CREATED AT ──────────────────────────────────────────────────────────
    // Timestamp recording when this user account was created.
    // "updatable = false" means once set, this value can never be changed.
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // ─── RELATIONSHIP: One User → Many Virtual Cards ─────────────────────────
    //
    // DATABASE RELATIONSHIP EXPLAINED:
    // One user can own MANY virtual cards (1-to-Many relationship).
    //
    // "mappedBy = user" means: the VirtualCard entity has a field called "user"
    //   that holds the foreign key. This side (User) does NOT own the FK column.
    //
    // "cascade = ALL" means: if we save/delete a User, all their cards are
    //   automatically saved/deleted too (like a parent dragging children along).
    //
    // "orphanRemoval = true" means: if we remove a card from this list,
    //   JPA will DELETE that card from the database (it's now an "orphan").
    //
    // @ToString.Exclude prevents infinite loops when printing:
    //   User.toString() → prints cards → each card prints User → infinite loop!
    @ToString.Exclude
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VirtualCard> virtualCards = new ArrayList<>();

    // ─── LIFECYCLE CALLBACK ──────────────────────────────────────────────────
    // @PrePersist = "Run this method JUST BEFORE saving to the database for the first time"
    // This automatically sets the creation timestamp — you don't need to set it manually.
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
