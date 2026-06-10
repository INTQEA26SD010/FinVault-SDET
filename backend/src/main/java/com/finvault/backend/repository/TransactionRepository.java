package com.finvault.backend.repository;

import com.finvault.backend.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;



@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    
    List<Transaction> findByVirtualCardIdOrderByTimestampDesc(Long virtualCardId);
}
