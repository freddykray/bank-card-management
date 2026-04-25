package com.example.bankcards.service.impl;

import com.example.bankcards.dto.user.response.CardBalanceResponseDTO;
import com.example.bankcards.dto.user.response.UserCardListResponseDTO;
import com.example.bankcards.dto.user.response.UserCardOneResponseDTO;
import com.example.bankcards.entity.Card;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.mapstruct.CardMapper;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.service.UserCardService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserCardServiceImpl implements UserCardService {

    private final CardRepository cardRepository;
    private final CardMapper cardMapper;

    @Override
    public UserCardListResponseDTO getMyCards() {
        return null;
    }

    @Override
    public UserCardOneResponseDTO getMyCardById(long id) {
        Card card = getCardIsNotDeletedByIdOrThrow(id);

        return cardMapper.toUserCardOneResponse(card);
    }

    @Override
    public CardBalanceResponseDTO getMyCardBalance(long id) {
        Card card = getCardIsNotDeletedByIdOrThrow(id);
        return cardMapper.toCardBalanceResponse(card);
    }

    @Override
    public UserCardOneResponseDTO requestCardBlock(long id) {

        return null;
    }

    private Card getCardIsNotDeletedByIdOrThrow(long id) {
        return cardRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("Карта не найдена! id=" + id));
    }
}
