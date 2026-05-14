package com.finvault.backend.service;

import com.finvault.backend.dto.VirtualCardResponseDto;
import com.finvault.backend.entity.VirtualCard;
import com.finvault.backend.repository.VirtualCardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Business logic for virtual card operations.
 * Maps VirtualCard entities to VirtualCardResponseDto objects,
 * ensuring sensitive fields (cvv, raw user FK) are never exposed.
 */
@Service
@RequiredArgsConstructor
public class VirtualCardService {

    private final VirtualCardRepository virtualCardRepository;

    /**
     * Retrieves all virtual cards belonging to the given user.
     * Maps each VirtualCard entity to a safe VirtualCardResponseDto
     * before returning — the CVV is never included in the response.
     *
     * @param userId the ID of the user whose cards should be fetched
     * @return list of card response DTOs (empty list if user has no cards)
     */
    public List<VirtualCardResponseDto> getCardsByUserId(Long userId) {
        List<VirtualCard> cards = virtualCardRepository.findByUserId(userId);

        return cards.stream()
                .map(card -> new VirtualCardResponseDto(
                        card.getId(),
                        card.getCardNumber(),
                        card.getDailyLimit(),
                        card.getStatus().name()
                ))
                .collect(Collectors.toList());
    }
}
