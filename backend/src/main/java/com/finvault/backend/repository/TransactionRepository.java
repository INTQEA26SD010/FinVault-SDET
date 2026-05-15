package com.finvault.backend.repository;

import com.finvault.backend.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for the Transaction entity.
 * Provides CRUD operations and custom finders for transaction records.
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    /**
     * Retrieves all transactions for a given virtual card, ordered by most recent first.
     */
    List<Transaction> findByVirtualCardIdOrderByTimestampDesc(Long virtualCardId);
}
