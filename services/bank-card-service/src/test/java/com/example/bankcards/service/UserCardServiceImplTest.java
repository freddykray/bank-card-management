package com.example.bankcards.service;

import com.example.bankcards.dto.PageResponseDTO;
import com.example.bankcards.dto.user.request.UserCardSearchRequestDTO;
import com.example.bankcards.dto.user.response.CardBalanceResponseDTO;
import com.example.bankcards.dto.user.response.UserCardOneResponseDTO;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.exception.ConflictException;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.mapstruct.CardMapper;
import com.example.bankcards.mapstruct.PageResponseMapper;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.security.CurrentUserService;
import com.example.bankcards.service.impl.user.UserCardServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
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

    @Mock
    private PageResponseMapper pageResponseMapper;

    @Test
    void getMyCards_success_returnsPageResponse() {
        UserCardSearchRequestDTO request = new UserCardSearchRequestDTO();
        request.setPage(0);
        request.setSize(10);
        request.setStatus(CardStatus.ACTIVE);
        request.setLast4("1234");

        long userId = 1L;

        Card card1 = new Card();
        card1.setId(1L);

        Card card2 = new Card();
        card2.setId(2L);

        Page<Card> cardsPage = new PageImpl<>(
                List.of(card1, card2),
                PageRequest.of(0, 10),
                2
        );

        PageResponseDTO<UserCardOneResponseDTO> responseDto =
                new PageResponseDTO<>(
                        List.of(),
                        0,
                        10,
                        2,
                        1,
                        true,
                        true
                );

        when(currentUserService.getCurrentUserId()).thenReturn(userId);

        when(cardRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(cardsPage);

        when(pageResponseMapper.toPageResponse(
                eq(cardsPage),
                ArgumentMatchers.<Function<Card, UserCardOneResponseDTO>>any()
        )).thenReturn(responseDto);

        PageResponseDTO<UserCardOneResponseDTO> result = userCardService.getMyCards(request);

        assertEquals(responseDto, result);

        verify(currentUserService).getCurrentUserId();
        verify(cardRepository).findAll(any(Specification.class), any(Pageable.class));
        verify(pageResponseMapper).toPageResponse(
                eq(cardsPage),
                ArgumentMatchers.<Function<Card, UserCardOneResponseDTO>>any()
        );
    }

    @Test
    void getMyCards_usesPageAndSizeFromRequest() {
        UserCardSearchRequestDTO request = new UserCardSearchRequestDTO();
        request.setPage(2);
        request.setSize(5);

        long userId = 1L;

        Page<Card> cardsPage = new PageImpl<>(
                List.of(),
                PageRequest.of(2, 5),
                0
        );

        PageResponseDTO<UserCardOneResponseDTO> responseDto =
                new PageResponseDTO<>(
                        List.of(),
                        2,
                        5,
                        0,
                        0,
                        false,
                        true
                );

        when(currentUserService.getCurrentUserId()).thenReturn(userId);

        when(cardRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(cardsPage);

        when(pageResponseMapper.toPageResponse(
                eq(cardsPage),
                ArgumentMatchers.<Function<Card, UserCardOneResponseDTO>>any()
        )).thenReturn(responseDto);

        PageResponseDTO<UserCardOneResponseDTO> result = userCardService.getMyCards(request);

        assertEquals(responseDto, result);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

        verify(cardRepository).findAll(any(Specification.class), pageableCaptor.capture());

        Pageable pageable = pageableCaptor.getValue();

        assertEquals(2, pageable.getPageNumber());
        assertEquals(5, pageable.getPageSize());

        verify(currentUserService).getCurrentUserId();
        verify(pageResponseMapper).toPageResponse(
                eq(cardsPage),
                ArgumentMatchers.<Function<Card, UserCardOneResponseDTO>>any()
        );
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