package com.finvault.backend.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;



@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // Auto-increment: 1, 2, 3...
    private Long id;

    
    @ToString.Exclude  // Prevent infinite toString() loop
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "virtual_card_id", nullable = false)
    private VirtualCard virtualCard;

    
    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    
    @Column(name = "merchant_name", nullable = false, length = 100)
    private String merchantName;

    
    @Column(name = "timestamp", nullable = false, updatable = false)
    private LocalDateTime timestamp;

   
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 10)
    private TransactionStatus status;

    
    @PrePersist
    protected void onCreate() {
        this.timestamp = LocalDateTime.now();
    }

    
    public enum TransactionStatus {
        SUCCESS,
        DECLINED
    }
}
