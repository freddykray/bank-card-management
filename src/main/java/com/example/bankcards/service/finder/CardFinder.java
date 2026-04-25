package com.example.bankcards.service.finder;

import com.example.bankcards.entity.Card;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.repository.CardRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class CardFinder {

    private final CardRepository cardRepository;

    public Card getOneByIdOrThrow(long id) {
        return cardRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Карта не найдена!"));
    }

    public Card getByIdAndUserIdOrThrow(long cardId, long userId) {
       return cardRepository.findByIdAndUserIdAndDeletedAtIsNull(cardId, userId)
                .orElseThrow(() -> new NotFoundException("Карта не найдена!"));
    }
}
