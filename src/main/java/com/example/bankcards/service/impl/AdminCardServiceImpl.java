package com.example.bankcards.service.impl;

import com.example.bankcards.dto.admin.request.CreateCardRequestDTO;
import com.example.bankcards.dto.admin.response.ListCardResponseDTO;
import com.example.bankcards.dto.admin.response.OneCardResponseDTO;
import com.example.bankcards.entity.Card;
import com.example.bankcards.mapstruct.CardMapper;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.service.AdminCardService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class AdminCardServiceImpl implements AdminCardService {

    private final CardRepository cardRepository;
    private final CardMapper cardMapper;

    @Override
    public ListCardResponseDTO getCards(boolean includeDeleted) {
        List<Card> cards = includeDeleted
                ? cardRepository.findAll()
                : cardRepository.findAllByDeletedAtIsNull();
        return cardMapper.toAdminCardListResponse(cards);

    }

    @Override
    public OneCardResponseDTO getCardById(long id) {
        return null;
    }

    @Override
    public OneCardResponseDTO createCard(CreateCardRequestDTO request) {
        return null;
    }

    @Override
    public OneCardResponseDTO blockCard(long id) {
        return null;
    }

    @Override
    public OneCardResponseDTO activateCard(long id) {
        return null;
    }

    @Override
    public void deleteCard(long id) {

    }

}
