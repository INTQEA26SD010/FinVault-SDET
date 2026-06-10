package com.finvault.backend.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;



@Entity
@Table(name = "virtual_cards")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VirtualCard {

    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // Auto-increment: 1, 2, 3...
    private Long id;

    
    @ToString.Exclude  // Prevents infinite loop: Card→User→Cards→User→...
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    
    @Column(name = "card_number", nullable = false, unique = true, length = 16)
    private String cardNumber;

   
    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    
    @Column(name = "cvv", nullable = false, length = 3)
    private String cvv;

    
    @Column(name = "daily_limit", nullable = false, precision = 10, scale = 2)
    private BigDecimal dailyLimit = BigDecimal.ZERO;

    
    @Column(name = "balance", nullable = false, precision = 10, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private CardStatus status = CardStatus.ACTIVE;

    
    @Column(name = "vendor_name", columnDefinition = "VARCHAR(100) NOT NULL DEFAULT ''")
    private String vendorName = "";

   
    @ToString.Exclude
    @OneToMany(mappedBy = "virtualCard", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Transaction> transactions = new ArrayList<>();

    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

   
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

   
    public enum CardStatus {
        ACTIVE,
        FROZEN,
        EXPIRED,
        CANCELLED
    }
}
