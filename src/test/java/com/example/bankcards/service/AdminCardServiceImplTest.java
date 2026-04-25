package com.example.bankcards.service;

import com.example.bankcards.dto.admin.request.CreateCardRequestDTO;
import com.example.bankcards.dto.admin.response.ListCardResponseDTO;
import com.example.bankcards.dto.admin.response.OneCardResponseDTO;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.exception.ConflictException;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.mapstruct.CardMapper;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.service.finder.CardFinder;
import com.example.bankcards.service.finder.UserFinder;
import com.example.bankcards.service.impl.AdminCardServiceImpl;
import com.example.bankcards.util.CardNumberEncryptor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
class AdminCardServiceImplTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private UserFinder userFinder;

    @Mock
    private CardMapper cardMapper;

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
    void createCard_success() {
        long userId = 1L;

        CreateCardRequestDTO request = new CreateCardRequestDTO();
        request.setUserId(userId);
        request.setCardNumber("1111222233334444");
        request.setOwnerName("REGULAR USER");
        request.setExpirationDate(LocalDate.of(2030, 12, 31));
        request.setInitialBalance(new BigDecimal("10000.00"));

        User user = new User();
        user.setId(userId);

        Card savedCard = new Card();
        savedCard.setId(10L);
        savedCard.setUser(user);
        savedCard.setEncryptedCardNumber("encrypted-card-number");
        savedCard.setCardNumberLast4("4444");
        savedCard.setOwnerName("REGULAR USER");
        savedCard.setExpirationDate(request.getExpirationDate());
        savedCard.setStatus(CardStatus.ACTIVE);
        savedCard.setBalance(request.getInitialBalance());
        savedCard.setBlockRequested(false);

        OneCardResponseDTO responseDto = new OneCardResponseDTO();
        responseDto.setId(10L);

        when(userFinder.getByIdOrThrow(userId)).thenReturn(user);
        when(encryptor.encrypt("1111222233334444")).thenReturn("encrypted-card-number");
        when(cardRepository.save(any(Card.class))).thenReturn(savedCard);
        when(cardMapper.toAdminCardResponse(savedCard)).thenReturn(responseDto);

        OneCardResponseDTO result = adminCardService.createCard(request);

        assertEquals(responseDto, result);

        verify(userFinder).getByIdOrThrow(userId);
        verify(encryptor).encrypt("1111222233334444");
        verify(cardRepository).save(any(Card.class));
        verify(cardMapper).toAdminCardResponse(savedCard);
    }

    @Test
    void createCard_userNotFound_throwsNotFoundException() {
        long userId = 999L;

        CreateCardRequestDTO request = new CreateCardRequestDTO();
        request.setUserId(userId);
        request.setCardNumber("1111222233334444");
        request.setOwnerName("REGULAR USER");
        request.setExpirationDate(LocalDate.of(2030, 12, 31));
        request.setInitialBalance(new BigDecimal("10000.00"));

        when(userFinder.getByIdOrThrow(userId))
                .thenThrow(new NotFoundException("Пользователь не найден"));

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> adminCardService.createCard(request)
        );

        assertEquals("Пользователь не найден", exception.getMessage());

        verify(userFinder).getByIdOrThrow(userId);
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