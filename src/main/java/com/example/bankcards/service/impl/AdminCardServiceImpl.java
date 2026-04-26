package com.example.bankcards.service.impl;

import com.example.bankcards.dto.PageResponseDTO;
import com.example.bankcards.dto.admin.request.AdminCardSearchRequestDTO;
import com.example.bankcards.dto.admin.request.CreateCardRequestDTO;
import com.example.bankcards.dto.admin.response.OneCardResponseDTO;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.GeneratedCardDetails;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.exception.ConflictException;
import com.example.bankcards.mapstruct.CardMapper;
import com.example.bankcards.mapstruct.PageResponseMapper;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.service.AdminCardService;
import com.example.bankcards.service.finder.CardFinder;
import com.example.bankcards.service.finder.UserFinder;
import com.example.bankcards.specification.AdminCardSpecification;
import com.example.bankcards.util.CardDetailsGenerator;
import com.example.bankcards.util.CardNumberEncryptor;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@AllArgsConstructor
@Slf4j
public class AdminCardServiceImpl implements AdminCardService {

    private final CardRepository cardRepository;
    private final UserFinder userFinder;
    private final CardMapper cardMapper;
    private final CardNumberEncryptor encryptor;
    private final CardFinder cardFinder;
    private final CardDetailsGenerator cardDetailsGenerator;
    private final PageResponseMapper pageResponseMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<OneCardResponseDTO> getCards(AdminCardSearchRequestDTO request) {
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());

        Page<Card> cardsPage = cardRepository.findAll(
                AdminCardSpecification.from(request),
                pageable
        );

        return pageResponseMapper.toPageResponse(
                cardsPage,
                cardMapper::toAdminCardResponse
        );
    }

    @Override
    @Transactional(readOnly = true)
    public OneCardResponseDTO getCardById(long id) {
        Card card = cardFinder.getOneByIdOrThrow(id);
        return cardMapper.toAdminCardResponse(card);
    }

    @Override
    @Transactional
    public OneCardResponseDTO createCard(CreateCardRequestDTO request) {
        User user = userFinder.getByIdOrThrow(request.getUserId());

        Card card = buildCard(request, user);

        Card savedCard = cardRepository.save(card);

        log.info("Карта создана: cardId={}, userId={}, last4={}",
                savedCard.getId(),
                user.getId(),
                savedCard.getCardNumberLast4()
        );

        return cardMapper.toAdminCardResponse(savedCard);
    }

    @Override
    @Transactional
    public OneCardResponseDTO blockCard(long id) {
        Card card = cardFinder.getOneByIdOrThrow(id);
        validateCardCanBeBlocked(card);
        card.setStatus(CardStatus.BLOCKED);
        card.setBlockRequested(false);
        card.setBlockRequestedAt(null);
        card.setUpdatedAt(Instant.now());
        log.info("Карта заблокирована: cardId={}", card.getId());
        return cardMapper.toAdminCardResponse(card);
    }

    @Override
    @Transactional
    public OneCardResponseDTO activateCard(long id) {
        Card card = cardFinder.getOneByIdOrThrow(id);
        validateCardCanBeActivated(card);
        card.setStatus(CardStatus.ACTIVE);
        card.setUpdatedAt(Instant.now());
        log.info("Карта активирована: cardId={}", card.getId());
        return cardMapper.toAdminCardResponse(card);
    }

    @Override
    @Transactional
    public void deleteCard(long id) {
        Card card = cardFinder.getOneByIdOrThrow(id);
        checkCardNotDeleted(card);
        card.setDeletedAt(Instant.now());
        card.setUpdatedAt(Instant.now());
        log.info("Карта логически удалена: cardId={}", card.getId());
    }

    private Card buildCard(CreateCardRequestDTO request, User user) {
        Instant now = Instant.now();

        GeneratedCardDetails generatedCardData =
                cardDetailsGenerator.generate();

        Card card = new Card();
        card.setEncryptedCardNumber(encryptor.encrypt(generatedCardData.cardNumber()));
        card.setCardNumberHash(generatedCardData.cardNumberHash());
        card.setCardNumberLast4(generatedCardData.last4());
        card.setOwnerName(request.getOwnerName());
        card.setExpirationDate(generatedCardData.expirationDate());
        card.setStatus(CardStatus.ACTIVE);
        card.setBalance(request.getInitialBalance());
        card.setBlockRequested(false);
        card.setUser(user);
        card.setCreatedAt(now);
        card.setUpdatedAt(now);

        return card;
    }

    private void validateCardCanBeBlocked(Card card) {
        checkCardNotDeleted(card);
        if (card.getStatus() == CardStatus.BLOCKED) {
            throw new ConflictException("Карта уже заблокирована");
        }
    }

    private void validateCardCanBeActivated(Card card) {
        checkCardNotDeleted(card);

        if (card.getStatus() == CardStatus.ACTIVE) {
            throw new ConflictException("Карта уже активна");
        }
        if (card.getStatus() == CardStatus.EXPIRED) {
            throw new ConflictException("Нельзя активировать карту с истёкшим сроком действия");
        }
    }

    private void checkCardNotDeleted(Card card) {
        if (card.getDeletedAt() != null) {
            throw new ConflictException("Карта уже удалена");
        }
    }
}
