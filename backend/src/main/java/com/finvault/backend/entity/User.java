package com.finvault.backend.entity;


import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;



@Entity          // Tells JPA: "This class is a database table — manage it for me"
@Table(name = "users")  // Specifies the exact table name in MySQL (otherwise JPA would use class name)

@Data            // Generates: getters, setters, toString(), equals(), hashCode() for ALL fields
@NoArgsConstructor   // Generates: an empty constructor → public User() {}   (JPA requires this)
@AllArgsConstructor  // Generates: a constructor with ALL fields as parameters
public class User {

   
    @Id  // Marks this field as the PRIMARY KEY of the table
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    
    private Long id;

    
    @Column(name = "username",    // Maps to the "username" column in the table
            nullable = false,      // NOT NULL — this field MUST have a value
            unique = true,         // UNIQUE — no two users can have the same username
            length = 50)           // VARCHAR(50) — max 50 characters in the database
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
