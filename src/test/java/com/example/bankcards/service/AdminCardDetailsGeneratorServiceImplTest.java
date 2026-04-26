package com.example.bankcards.service;

import com.example.bankcards.dto.admin.request.CreateCardRequestDTO;
import com.example.bankcards.dto.admin.response.ListCardResponseDTO;
import com.example.bankcards.dto.admin.response.OneCardResponseDTO;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.GeneratedCardDetails;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.exception.ConflictException;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.mapstruct.CardMapper;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.service.finder.CardFinder;
import com.example.bankcards.service.finder.UserFinder;
import com.example.bankcards.service.impl.AdminCardServiceImpl;
import com.example.bankcards.util.CardDetailsGenerator;
import com.example.bankcards.util.CardNumberEncryptor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminCardDetailsGeneratorServiceImplTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private UserFinder userFinder;

    @Mock
    private CardMapper cardMapper;

    @Mock
    private CardDetailsGenerator cardDetailsGenerator;

    @Mock
    private CardNumberEncryptor encryptor;

    @Mock
    private CardFinder cardFinder;

    @InjectMocks
    private AdminCardServiceImpl adminCardService;

    @Test
    void getCards_includeDeletedTrue_success() {
        Card card1 = new Card();
        card1.setId(1L);

        Card card2 = new Card();
        card2.setId(2L);

        List<Card> cards = List.of(card1, card2);

        ListCardResponseDTO responseDto = new ListCardResponseDTO();

        when(cardRepository.findAll()).thenReturn(cards);
        when(cardMapper.toAdminCardListResponse(cards)).thenReturn(responseDto);

        ListCardResponseDTO result = adminCardService.getCards(true);

        assertEquals(responseDto, result);

        verify(cardRepository).findAll();
        verify(cardRepository, never()).findAllByDeletedAtIsNull();
        verify(cardMapper).toAdminCardListResponse(cards);
    }

    @Test
    void getCards_includeDeletedFalse_success() {
        Card card1 = new Card();
        card1.setId(1L);

        Card card2 = new Card();
        card2.setId(2L);

        List<Card> cards = List.of(card1, card2);

        ListCardResponseDTO responseDto = new ListCardResponseDTO();

        when(cardRepository.findAllByDeletedAtIsNull()).thenReturn(cards);
        when(cardMapper.toAdminCardListResponse(cards)).thenReturn(responseDto);

        ListCardResponseDTO result = adminCardService.getCards(false);

        assertEquals(responseDto, result);

        verify(cardRepository).findAllByDeletedAtIsNull();
        verify(cardRepository, never()).findAll();
        verify(cardMapper).toAdminCardListResponse(cards);
    }

    @Test
    void getCardById_success() {
        long cardId = 1L;

        Card card = new Card();
        card.setId(cardId);

        OneCardResponseDTO responseDto = new OneCardResponseDTO();
        responseDto.setId(cardId);

        when(cardFinder.getOneByIdOrThrow(cardId)).thenReturn(card);
        when(cardMapper.toAdminCardResponse(card)).thenReturn(responseDto);

        OneCardResponseDTO result = adminCardService.getCardById(cardId);

        assertEquals(responseDto, result);

        verify(cardFinder).getOneByIdOrThrow(cardId);
        verify(cardMapper).toAdminCardResponse(card);
    }

    @Test
    void getCardById_notFound_throwsNotFoundException() {
        long cardId = 999L;

        when(cardFinder.getOneByIdOrThrow(cardId))
                .thenThrow(new NotFoundException("Карта не найдена"));

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> adminCardService.getCardById(cardId)
        );

        assertEquals("Карта не найдена", exception.getMessage());

        verify(cardFinder).getOneByIdOrThrow(cardId);
        verify(cardMapper, never()).toAdminCardResponse(any(Card.class));
    }

    @Test
    void createCard_success_savesGeneratedCardData() {
        long userId = 1L;

        CreateCardRequestDTO request = new CreateCardRequestDTO();
        request.setUserId(userId);
        request.setOwnerName("REGULAR USER");
        request.setInitialBalance(new BigDecimal("10000.00"));

        User user = new User();
        user.setId(userId);

        GeneratedCardDetails generatedCardDetails =
                new GeneratedCardDetails(
                        "1111222233334444",
                        "card-number-hash",
                        "4444",
                        LocalDate.of(2031, 4, 26)
                );

        OneCardResponseDTO responseDto = new OneCardResponseDTO();
        responseDto.setId(10L);

        when(userFinder.getByIdOrThrow(userId)).thenReturn(user);
        when(cardDetailsGenerator.generate()).thenReturn(generatedCardDetails);
        when(encryptor.encrypt("1111222233334444")).thenReturn("encrypted-card-number");
        when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> {
            Card card = invocation.getArgument(0);
            card.setId(10L);
            return card;
        });
        when(cardMapper.toAdminCardResponse(any(Card.class))).thenReturn(responseDto);

        OneCardResponseDTO result = adminCardService.createCard(request);

        assertEquals(responseDto, result);

        ArgumentCaptor<Card> cardCaptor = ArgumentCaptor.forClass(Card.class);
        verify(cardRepository).save(cardCaptor.capture());

        Card savedCard = cardCaptor.getValue();

        assertEquals("encrypted-card-number", savedCard.getEncryptedCardNumber());
        assertEquals("card-number-hash", savedCard.getCardNumberHash());
        assertEquals("4444", savedCard.getCardNumberLast4());
        assertEquals("REGULAR USER", savedCard.getOwnerName());
        assertEquals(LocalDate.of(2031, 4, 26), savedCard.getExpirationDate());
        assertEquals(CardStatus.ACTIVE, savedCard.getStatus());
        assertEquals(new BigDecimal("10000.00"), savedCard.getBalance());
        assertFalse(savedCard.isBlockRequested());
        assertEquals(user, savedCard.getUser());
        assertNotNull(savedCard.getCreatedAt());
        assertNotNull(savedCard.getUpdatedAt());

        verify(cardDetailsGenerator).generate();
        verify(encryptor).encrypt("1111222233334444");
    }

    @Test
    void createCard_userNotFound_throwsNotFoundException() {
        long userId = 999L;

        CreateCardRequestDTO request = new CreateCardRequestDTO();
        request.setUserId(userId);
        request.setOwnerName("REGULAR USER");
        request.setInitialBalance(new BigDecimal("10000.00"));

        when(userFinder.getByIdOrThrow(userId))
                .thenThrow(new NotFoundException("Пользователь не найден"));

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> adminCardService.createCard(request)
        );

        assertEquals("Пользователь не найден", exception.getMessage());

        verify(userFinder).getByIdOrThrow(userId);
        verify(cardDetailsGenerator, never()).generate();
        verify(encryptor, never()).encrypt(any(String.class));
        verify(cardRepository, never()).save(any(Card.class));
        verify(cardMapper, never()).toAdminCardResponse(any(Card.class));
    }

    @Test
    void createCard_cardDetailsGenerationFailed_throwsConflictException() {
        long userId = 1L;

        CreateCardRequestDTO request = new CreateCardRequestDTO();
        request.setUserId(userId);
        request.setOwnerName("REGULAR USER");
        request.setInitialBalance(new BigDecimal("10000.00"));

        User user = new User();
        user.setId(userId);

        when(userFinder.getByIdOrThrow(userId)).thenReturn(user);
        when(cardDetailsGenerator.generate())
                .thenThrow(new ConflictException("Не удалось сгенерировать уникальный номер карты"));

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> adminCardService.createCard(request)
        );

        assertEquals("Не удалось сгенерировать уникальный номер карты", exception.getMessage());

        verify(userFinder).getByIdOrThrow(userId);
        verify(cardDetailsGenerator).generate();
        verify(encryptor, never()).encrypt(any(String.class));
        verify(cardRepository, never()).save(any(Card.class));
        verify(cardMapper, never()).toAdminCardResponse(any(Card.class));
    }

    @Test
    void blockCard_success() {
        long cardId = 1L;

        Card card = new Card();
        card.setId(cardId);
        card.setStatus(CardStatus.ACTIVE);
        card.setBlockRequested(true);
        card.setBlockRequestedAt(Instant.now());

        OneCardResponseDTO responseDto = new OneCardResponseDTO();
        responseDto.setId(cardId);

        when(cardFinder.getOneByIdOrThrow(cardId)).thenReturn(card);
        when(cardMapper.toAdminCardResponse(card)).thenReturn(responseDto);

        OneCardResponseDTO result = adminCardService.blockCard(cardId);

        assertEquals(responseDto, result);
        assertEquals(CardStatus.BLOCKED, card.getStatus());
        assertFalse(card.isBlockRequested());
        assertNull(card.getBlockRequestedAt());
        assertNotNull(card.getUpdatedAt());

        verify(cardFinder).getOneByIdOrThrow(cardId);
        verify(cardMapper).toAdminCardResponse(card);
    }

    @Test
    void blockCard_deletedCard_throwsConflictException() {
        long cardId = 1L;

        Card card = new Card();
        card.setId(cardId);
        card.setStatus(CardStatus.ACTIVE);
        card.setDeletedAt(Instant.now());

        when(cardFinder.getOneByIdOrThrow(cardId)).thenReturn(card);

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> adminCardService.blockCard(cardId)
        );

        assertEquals("Карта уже удалена", exception.getMessage());

        verify(cardFinder).getOneByIdOrThrow(cardId);
        verify(cardMapper, never()).toAdminCardResponse(any(Card.class));
    }

    @Test
    void blockCard_alreadyBlocked_throwsConflictException() {
        long cardId = 1L;

        Card card = new Card();
        card.setId(cardId);
        card.setStatus(CardStatus.BLOCKED);

        when(cardFinder.getOneByIdOrThrow(cardId)).thenReturn(card);

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> adminCardService.blockCard(cardId)
        );

        assertEquals("Карта уже заблокирована", exception.getMessage());

        verify(cardFinder).getOneByIdOrThrow(cardId);
        verify(cardMapper, never()).toAdminCardResponse(any(Card.class));
    }

    @Test
    void activateCard_success() {
        long cardId = 1L;

        Card card = new Card();
        card.setId(cardId);
        card.setStatus(CardStatus.BLOCKED);

        OneCardResponseDTO responseDto = new OneCardResponseDTO();
        responseDto.setId(cardId);

        when(cardFinder.getOneByIdOrThrow(cardId)).thenReturn(card);
        when(cardMapper.toAdminCardResponse(card)).thenReturn(responseDto);

        OneCardResponseDTO result = adminCardService.activateCard(cardId);

        assertEquals(responseDto, result);
        assertEquals(CardStatus.ACTIVE, card.getStatus());
        assertNotNull(card.getUpdatedAt());

        verify(cardFinder).getOneByIdOrThrow(cardId);
        verify(cardMapper).toAdminCardResponse(card);
    }

    @Test
    void activateCard_alreadyActive_throwsConflictException() {
        long cardId = 1L;

        Card card = new Card();
        card.setId(cardId);
        card.setStatus(CardStatus.ACTIVE);

        when(cardFinder.getOneByIdOrThrow(cardId)).thenReturn(card);

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> adminCardService.activateCard(cardId)
        );

        assertEquals("Карта уже активна", exception.getMessage());

        verify(cardFinder).getOneByIdOrThrow(cardId);
        verify(cardMapper, never()).toAdminCardResponse(any(Card.class));
    }

    @Test
    void activateCard_deletedCard_throwsConflictException() {
        long cardId = 1L;

        Card card = new Card();
        card.setId(cardId);
        card.setStatus(CardStatus.BLOCKED);
        card.setDeletedAt(Instant.now());

        when(cardFinder.getOneByIdOrThrow(cardId)).thenReturn(card);

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> adminCardService.activateCard(cardId)
        );

        assertEquals("Карта уже удалена", exception.getMessage());

        verify(cardFinder).getOneByIdOrThrow(cardId);
        verify(cardMapper, never()).toAdminCardResponse(any(Card.class));
    }

    @Test
    void activateCard_expiredCard_throwsConflictException() {
        long cardId = 1L;

        Card card = new Card();
        card.setId(cardId);
        card.setStatus(CardStatus.EXPIRED);

        when(cardFinder.getOneByIdOrThrow(cardId)).thenReturn(card);

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> adminCardService.activateCard(cardId)
        );

        assertEquals("Нельзя активировать карту с истёкшим сроком действия", exception.getMessage());

        verify(cardFinder).getOneByIdOrThrow(cardId);
        verify(cardMapper, never()).toAdminCardResponse(any(Card.class));
    }

    @Test
    void deleteCard_success_setsDeletedAt() {
        long cardId = 1L;

        Card card = new Card();
        card.setId(cardId);
        card.setStatus(CardStatus.ACTIVE);

        when(cardFinder.getOneByIdOrThrow(cardId)).thenReturn(card);

        adminCardService.deleteCard(cardId);

        assertNotNull(card.getDeletedAt());
        assertNotNull(card.getUpdatedAt());

        verify(cardFinder).getOneByIdOrThrow(cardId);
    }

    @Test
    void deleteCard_alreadyDeleted_throwsConflictException() {
        long cardId = 1L;

        Card card = new Card();
        card.setId(cardId);
        card.setDeletedAt(Instant.now());

        when(cardFinder.getOneByIdOrThrow(cardId)).thenReturn(card);

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> adminCardService.deleteCard(cardId)
        );

        assertEquals("Карта уже удалена", exception.getMessage());

        verify(cardFinder).getOneByIdOrThrow(cardId);
        verify(cardRepository, never()).delete(any(Card.class));
    }

    @Test
    void getBlockRequestedCards_success() {
        Card card1 = new Card();
        card1.setId(1L);
        card1.setBlockRequested(true);

        Card card2 = new Card();
        card2.setId(2L);
        card2.setBlockRequested(true);

        List<Card> cards = List.of(card1, card2);

        ListCardResponseDTO responseDto = new ListCardResponseDTO();

        when(cardRepository.findAllByBlockRequestedTrueAndDeletedAtIsNull())
                .thenReturn(cards);

        when(cardMapper.toAdminCardListResponse(cards))
                .thenReturn(responseDto);

        ListCardResponseDTO result = adminCardService.getBlockRequestedCards();

        assertEquals(responseDto, result);

        verify(cardRepository).findAllByBlockRequestedTrueAndDeletedAtIsNull();
        verify(cardMapper).toAdminCardListResponse(cards);
    }
}