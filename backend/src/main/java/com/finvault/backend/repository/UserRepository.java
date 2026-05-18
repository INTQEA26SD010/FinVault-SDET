package com.finvault.backend.repository;

import com.finvault.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

// ─────────────────────────────────────────────────────────────────────────────
// USER REPOSITORY — The database access layer for the "users" table.
// ─────────────────────────────────────────────────────────────────────────────
//
// WHAT IS A REPOSITORY?
// A Repository is an interface that provides methods to interact with the
// database (CRUD = Create, Read, Update, Delete). You define the METHOD NAME,
// and Spring Data JPA automatically WRITES THE SQL QUERY for you!
//
// HOW DOES IT WORK? (The Magic of Spring Data JPA)
// 1. You create an interface that extends JpaRepository<EntityType, IdType>
// 2. Spring auto-generates the implementation class at runtime
// 3. You get these methods FOR FREE (no code needed):
//      - save(entity)       → INSERT or UPDATE
//      - findById(id)       → SELECT * WHERE id = ?
//      - findAll()          → SELECT * FROM users
//      - deleteById(id)     → DELETE FROM users WHERE id = ?
//      - count()            → SELECT COUNT(*) FROM users
//      - existsById(id)     → SELECT COUNT(*) > 0 WHERE id = ?
//
// CUSTOM QUERIES — DERIVED METHOD NAMING:
// Spring reads your method name and builds the query:
//   findByEmail(email)    → SELECT * FROM users WHERE email = ?
//   existsByEmail(email)  → SELECT COUNT(*) > 0 FROM users WHERE email = ?
//   findByUsername(name)  → SELECT * FROM users WHERE username = ?
//
// HOW IT FITS IN THE PROJECT:
// Controller → Service → THIS REPOSITORY → Database
// The Service calls repository methods. The repository talks to MySQL.
//
// ─────────────────────────────────────────────────────────────────────────────

@Repository  // Marks this interface as a Spring Bean (auto-detected by Spring)
public interface UserRepository extends JpaRepository<User, Long> {
    // ↑ JpaRepository<User, Long> means:
    //   - User = the Entity this repository manages
    //   - Long = the type of the primary key (User.id is Long)

    // ─── FIND BY EMAIL ───────────────────────────────────────────────────────
    // Used during LOGIN: find the user with this email address.
    //
    // Returns Optional<User>:
    //   - Optional.of(user) if found
    //   - Optional.empty() if no user has this email
    //
    // Optional is Java's way of saying "this might be null" — it forces you
    // to handle the "not found" case explicitly, preventing NullPointerException.
    //
    // Generated SQL: SELECT * FROM users WHERE email = ?
    Optional<User> findByEmail(String email);

    // ─── FIND BY USERNAME ────────────────────────────────────────────────────
    // Used to look up users by their display name.
    // Generated SQL: SELECT * FROM users WHERE username = ?
    Optional<User> findByUsername(String username);

    // ─── EXISTS BY EMAIL ─────────────────────────────────────────────────────
    // Used during REGISTRATION to check if an email is already taken.
    // Returns true/false — more efficient than loading the full User entity
    // when you just need a yes/no answer.
    //
    // Generated SQL: SELECT COUNT(*) > 0 FROM users WHERE email = ?
    boolean existsByEmail(String email);
}
