package com.finvault.backend.repository;

import com.finvault.backend.entity.VirtualCard;
import com.finvault.backend.entity.VirtualCard.CardStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;



@Repository
public interface VirtualCardRepository extends JpaRepository<VirtualCard, Long> {

   
    List<VirtualCard> findByUserId(Long userId);

   
    Optional<VirtualCard> findByCardNumber(String cardNumber);

    
    List<VirtualCard> findByUserIdAndStatus(Long userId, CardStatus status);
}
