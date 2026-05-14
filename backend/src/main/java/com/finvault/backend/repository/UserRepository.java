package com.finvault.backend.repository;

import com.finvault.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for the User entity.
 * Provides full CRUD operations inherited from JpaRepository<User, Long>.
 * Custom finders follow Spring Data's derived query method naming convention —
 * no SQL required.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds a user by their unique email address.
     * Used during authentication to load UserDetails by email.
     * Spring Data generates: SELECT * FROM users WHERE email = ?
     */
    Optional<User> findByEmail(String email);

    /**
     * Finds a user by their unique username.
     * Used for duplicate-check during registration.
     * Spring Data generates: SELECT * FROM users WHERE username = ?
     */
    Optional<User> findByUsername(String username);

    /**
     * Checks whether a user exists with the given email — avoids
     * loading the full entity when only a boolean is needed.
     * Spring Data generates: SELECT COUNT(*) > 0 FROM users WHERE email = ?
     */
    boolean existsByEmail(String email);
}
