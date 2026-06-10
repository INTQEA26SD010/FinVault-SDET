package com.finvault.backend.repository;

import com.finvault.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;



@Repository  // Marks this interface as a Spring Bean (auto-detected by Spring)
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByEmail(String email);

   
    Optional<User> findByUsername(String username);

   
    boolean existsByEmail(String email);
}
