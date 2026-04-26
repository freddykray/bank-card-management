package com.example.bankcards.service;

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
import com.example.bankcards.service.impl.UserCardServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserCardServiceImplTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private CardMapper cardMapper;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private UserCardServiceImpl userCardService;

    @Test
    void getMyCards_success() {
        long currentUserId = 1L;

        Card card1 = new Card();
        card1.setId(10L);

        Card card2 = new Card();
        card2.setId(20L);

        List<Card> cards = List.of(card1, card2);

        UserCardListResponseDTO responseDto = new UserCardListResponseDTO();

        when(currentUserService.getCurrentUserId()).thenReturn(currentUserId);
        when(cardRepository.findAllByUserIdAndDeletedAtIsNull(currentUserId)).thenReturn(cards);
        when(cardMapper.toUserCardListResponse(cards)).thenReturn(responseDto);

        UserCardListResponseDTO result = userCardService.getMyCards();

        assertEquals(responseDto, result);

        verify(currentUserService).getCurrentUserId();
        verify(cardRepository).findAllByUserIdAndDeletedAtIsNull(currentUserId);
        verify(cardMapper).toUserCardListResponse(cards);
    }

    @Test
    void getMyCardById_success() {
        long currentUserId = 1L;
        long cardId = 10L;

        Card card = new Card();
        card.setId(cardId);

        UserCardOneResponseDTO responseDto = new UserCardOneResponseDTO();
        responseDto.setId(cardId);

        when(currentUserService.getCurrentUserId()).thenReturn(currentUserId);
        when(cardRepository.findByIdAndUserIdAndDeletedAtIsNull(cardId, currentUserId))
                .thenReturn(Optional.of(card));
        when(cardMapper.toUserCardOneResponse(card)).thenReturn(responseDto);

        UserCardOneResponseDTO result = userCardService.getMyCardById(cardId);

        assertEquals(responseDto, result);

        verify(currentUserService).getCurrentUserId();
        verify(cardRepository).findByIdAndUserIdAndDeletedAtIsNull(cardId, currentUserId);
        verify(cardMapper).toUserCardOneResponse(card);
    }

    @Test
    void getMyCardById_notFound_throwsNotFoundException() {
        long currentUserId = 1L;
        long cardId = 999L;

        when(currentUserService.getCurrentUserId()).thenReturn(currentUserId);
        when(cardRepository.findByIdAndUserIdAndDeletedAtIsNull(cardId, currentUserId))
                .thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> userCardService.getMyCardById(cardId)
        );

        assertEquals("Карта не найдена", exception.getMessage());

        verify(currentUserService).getCurrentUserId();
        verify(cardRepository).findByIdAndUserIdAndDeletedAtIsNull(cardId, currentUserId);
    }

    @Test
    void getMyCardBalance_success() {
        long currentUserId = 1L;
        long cardId = 10L;

        Card card = new Card();
        card.setId(cardId);
        card.setStatus(CardStatus.BLOCKED);

        CardBalanceResponseDTO responseDto = new CardBalanceResponseDTO();

        when(currentUserService.getCurrentUserId()).thenReturn(currentUserId);
        when(cardRepository.findByIdAndUserIdAndDeletedAtIsNull(cardId, currentUserId))
                .thenReturn(Optional.of(card));
        when(cardMapper.toCardBalanceResponse(card)).thenReturn(responseDto);

        CardBalanceResponseDTO result = userCardService.getMyCardBalance(cardId);

        assertEquals(responseDto, result);

        verify(currentUserService).getCurrentUserId();
        verify(cardRepository).findByIdAndUserIdAndDeletedAtIsNull(cardId, currentUserId);
        verify(cardMapper).toCardBalanceResponse(card);
    }

    @Test
    void getMyCardBalance_notFound_throwsNotFoundException() {
        long currentUserId = 1L;
        long cardId = 999L;

        when(currentUserService.getCurrentUserId()).thenReturn(currentUserId);
        when(cardRepository.findByIdAndUserIdAndDeletedAtIsNull(cardId, currentUserId))
                .thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> userCardService.getMyCardBalance(cardId)
        );

        assertEquals("Карта не найдена", exception.getMessage());

        verify(currentUserService).getCurrentUserId();
        verify(cardRepository).findByIdAndUserIdAndDeletedAtIsNull(cardId, currentUserId);
        verify(cardMapper, never()).toCardBalanceResponse(any(Card.class));
    }

    @Test
    void requestCardBlock_success() {
        long currentUserId = 1L;
        long cardId = 10L;

        Card card = new Card();
        card.setId(cardId);
        card.setStatus(CardStatus.ACTIVE);
        card.setBlockRequested(false);

        UserCardOneResponseDTO responseDto = new UserCardOneResponseDTO();
        responseDto.setId(cardId);

        when(currentUserService.getCurrentUserId()).thenReturn(currentUserId);
        when(cardRepository.findByIdAndUserIdAndDeletedAtIsNull(cardId, currentUserId))
                .thenReturn(Optional.of(card));
        when(cardMapper.toUserCardOneResponse(card)).thenReturn(responseDto);

        UserCardOneResponseDTO result = userCardService.requestCardBlock(cardId);

        assertEquals(responseDto, result);
        assertTrue(card.isBlockRequested());
        assertNotNull(card.getBlockRequestedAt());
        assertNotNull(card.getUpdatedAt());

        verify(cardMapper).toUserCardOneResponse(card);
    }

    @Test
    void requestCardBlock_blockedCard_throwsConflictException() {
        long currentUserId = 1L;
        long cardId = 10L;

        Card card = new Card();
        card.setId(cardId);
        card.setStatus(CardStatus.BLOCKED);
        card.setBlockRequested(false);

        when(currentUserService.getCurrentUserId()).thenReturn(currentUserId);
        when(cardRepository.findByIdAndUserIdAndDeletedAtIsNull(cardId, currentUserId))
                .thenReturn(Optional.of(card));

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> userCardService.requestCardBlock(cardId)
        );

        assertEquals("Карта уже заблокирована", exception.getMessage());

        verify(cardMapper, never()).toUserCardOneResponse(any(Card.class));
    }

    @Test
    void requestCardBlock_alreadyRequested_throwsConflictException() {
        long currentUserId = 1L;
        long cardId = 10L;

        Card card = new Card();
        card.setId(cardId);
        card.setStatus(CardStatus.ACTIVE);
        card.setBlockRequested(true);

        when(currentUserService.getCurrentUserId()).thenReturn(currentUserId);
        when(cardRepository.findByIdAndUserIdAndDeletedAtIsNull(cardId, currentUserId))
                .thenReturn(Optional.of(card));

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> userCardService.requestCardBlock(cardId)
        );

        assertEquals("Запрос на блокировку карты уже создан", exception.getMessage());

        verify(cardMapper, never()).toUserCardOneResponse(any(Card.class));
    }

    @Test
    void requestCardBlock_notFound_throwsNotFoundException() {
        long currentUserId = 1L;
        long cardId = 999L;

        when(currentUserService.getCurrentUserId()).thenReturn(currentUserId);
        when(cardRepository.findByIdAndUserIdAndDeletedAtIsNull(cardId, currentUserId))
                .thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> userCardService.requestCardBlock(cardId)
        );

        assertEquals("Карта не найдена", exception.getMessage());

        verify(cardMapper, never()).toUserCardOneResponse(any(Card.class));
    }
}