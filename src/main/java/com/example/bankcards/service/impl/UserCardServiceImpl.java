package com.example.bankcards.service.impl;

import com.example.bankcards.dto.user.response.CardBalanceResponseDTO;
import com.example.bankcards.dto.user.response.UserCardListResponseDTO;
import com.example.bankcards.dto.user.response.UserCardOneResponseDTO;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.exception.ConflictException;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.mapstruct.CardMapper;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.security.CurrentUserService;
import com.example.bankcards.service.UserCardService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@AllArgsConstructor
public class UserCardServiceImpl implements UserCardService {

    private final CardRepository cardRepository;
    private final CardMapper cardMapper;
    private final CurrentUserService currentUserService;

    @Override
    @Transactional(readOnly = true)
    public UserCardListResponseDTO getMyCards() {
        long userId = getUserIdFromContext();
        List<Card> cardList = cardRepository.findAllByUserIdAndDeletedAtIsNull(userId);
        return cardMapper.toUserCardListResponse(cardList);
    }

    @Override
    @Transactional(readOnly = true)
    public UserCardOneResponseDTO getMyCardById(long id) {
        Card card = getCardByIdAndUserIdAndDeletedAtIsNull(id);
        return cardMapper.toUserCardOneResponse(card);
    }

    @Override
    @Transactional(readOnly = true)
    public CardBalanceResponseDTO getMyCardBalance(long id) {
        Card card = getCardByIdAndUserIdAndDeletedAtIsNull(id);
        return cardMapper.toCardBalanceResponse(card);
    }

    @Override
    @Transactional
    public UserCardOneResponseDTO requestCardBlock(long id) {

        Card card = getCardByIdAndUserIdAndDeletedAtIsNull(id);
        validateCardCanRequestBlock(card);
        Instant now = Instant.now();
        card.setBlockRequested(true);
        card.setUpdatedAt(now);
        card.setBlockRequestedAt(now);
        return cardMapper.toUserCardOneResponse(card);
    }

    private Card getCardByIdAndUserIdAndDeletedAtIsNull(long id) {
        long userId = getUserIdFromContext();
        return cardRepository.findByIdAndUserIdAndDeletedAtIsNull(id, userId)
                .orElseThrow(() -> new NotFoundException("У пользователя с id=" + userId + " не найдена карта id=" + id));
    }

    private long getUserIdFromContext() {
        return currentUserService.getCurrentUserId();
    }

    private void validateCardCanRequestBlock(Card card) {
        if (card.getStatus() == CardStatus.BLOCKED) {
            throw new ConflictException("Карта уже заблокирована");
        }
        if (card.isBlockRequested()) {
            throw new ConflictException("Запрос на блокировку карты уже создан");
        }
    }

}
