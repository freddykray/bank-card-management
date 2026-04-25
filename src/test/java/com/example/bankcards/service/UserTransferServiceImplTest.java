package com.example.bankcards.service;

import com.example.bankcards.dto.user.request.CreateTransferRequestDTO;
import com.example.bankcards.dto.user.response.OneTransferResponseDTO;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.Transfer;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.entity.enums.TransferStatus;
import com.example.bankcards.exception.ConflictException;
import com.example.bankcards.mapstruct.TransferMapper;
import com.example.bankcards.repository.TransferRepository;
import com.example.bankcards.security.CurrentUserService;
import com.example.bankcards.service.finder.CardFinder;
import com.example.bankcards.service.impl.UserTransferServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class UserTransferServiceImplTest {

    @Mock
    private TransferRepository transferRepository;

    @Mock
    private TransferMapper transferMapper;

    @Mock
    private CardFinder cardFinder;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private UserTransferServiceImpl userTransferService;

    @Test
    void createTransfer_success() {
        long currentUserId = 1L;

        CreateTransferRequestDTO request = new CreateTransferRequestDTO();
        request.setFromCardId(10L);
        request.setToCardId(20L);
        request.setAmount(new BigDecimal("200.00"));

        User user = new User();
        user.setId(currentUserId);

        Card fromCard = new Card();
        fromCard.setId(10L);
        fromCard.setUser(user);
        fromCard.setStatus(CardStatus.ACTIVE);
        fromCard.setBalance(new BigDecimal("1000.00"));

        Card toCard = new Card();
        toCard.setId(20L);
        toCard.setUser(user);
        toCard.setStatus(CardStatus.ACTIVE);
        toCard.setBalance(new BigDecimal("500.00"));

        Transfer savedTransfer = new Transfer();
        savedTransfer.setId(100L);
        savedTransfer.setFromCard(fromCard);
        savedTransfer.setToCard(toCard);
        savedTransfer.setAmount(request.getAmount());
        savedTransfer.setStatus(TransferStatus.SUCCESS);

        OneTransferResponseDTO responseDto = new OneTransferResponseDTO();
        responseDto.setId(100L);

        when(currentUserService.getCurrentUserId()).thenReturn(currentUserId);
        when(cardFinder.getByIdAndUserIdOrThrow(10L, currentUserId)).thenReturn(fromCard);
        when(cardFinder.getByIdAndUserIdOrThrow(20L, currentUserId)).thenReturn(toCard);
        when(transferRepository.save(any(Transfer.class))).thenReturn(savedTransfer);
        when(transferMapper.toTransferResponse(savedTransfer)).thenReturn(responseDto);

        OneTransferResponseDTO result = userTransferService.createTransfer(request);

        assertEquals(100L, result.getId());
        assertEquals(new BigDecimal("800.00"), fromCard.getBalance());
        assertEquals(new BigDecimal("700.00"), toCard.getBalance());
        verify(transferRepository).save(any(Transfer.class));
        verify(transferMapper).toTransferResponse(savedTransfer);
    }

    @Test
    void createTransfer_sameCard_throwsConflictException() {
        long currentUserId = 1L;

        CreateTransferRequestDTO request = new CreateTransferRequestDTO();
        request.setFromCardId(10L);
        request.setToCardId(10L);
        request.setAmount(new BigDecimal("200.00"));

        User user = new User();
        user.setId(currentUserId);

        Card card = new Card();
        card.setId(10L);
        card.setUser(user);
        card.setStatus(CardStatus.ACTIVE);
        card.setBalance(new BigDecimal("1000.00"));

        when(currentUserService.getCurrentUserId()).thenReturn(currentUserId);
        when(cardFinder.getByIdAndUserIdOrThrow(10L, currentUserId)).thenReturn(card);

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> userTransferService.createTransfer(request)
        );

        assertEquals("Нельзя выполнить перевод на ту же самую карту", exception.getMessage());

        verify(transferRepository, never()).save(any(Transfer.class));
    }

    @Test
    void createTransfer_inactiveCard_throwsConflictException() {
        long currentUserId = 1L;

        CreateTransferRequestDTO request = new CreateTransferRequestDTO();
        request.setFromCardId(10L);
        request.setToCardId(20L);
        request.setAmount(new BigDecimal("200.00"));

        User user = new User();
        user.setId(currentUserId);

        Card fromCard = new Card();
        fromCard.setId(10L);
        fromCard.setUser(user);
        fromCard.setStatus(CardStatus.BLOCKED);
        fromCard.setBalance(new BigDecimal("1000.00"));

        Card toCard = new Card();
        toCard.setId(20L);
        toCard.setUser(user);
        toCard.setStatus(CardStatus.ACTIVE);
        toCard.setBalance(new BigDecimal("500.00"));

        when(currentUserService.getCurrentUserId()).thenReturn(currentUserId);
        when(cardFinder.getByIdAndUserIdOrThrow(10L, currentUserId)).thenReturn(fromCard);
        when(cardFinder.getByIdAndUserIdOrThrow(20L, currentUserId)).thenReturn(toCard);

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> userTransferService.createTransfer(request)
        );

        assertEquals("Перевод возможен только между активными картами", exception.getMessage());

        verify(transferRepository, never()).save(any(Transfer.class));
    }

    @Test
    void createTransfer_notEnoughBalance_throwsConflictException() {
        long currentUserId = 1L;

        CreateTransferRequestDTO request = new CreateTransferRequestDTO();
        request.setFromCardId(10L);
        request.setToCardId(20L);
        request.setAmount(new BigDecimal("2000.00"));

        User user = new User();
        user.setId(currentUserId);

        Card fromCard = new Card();
        fromCard.setId(10L);
        fromCard.setUser(user);
        fromCard.setStatus(CardStatus.ACTIVE);
        fromCard.setBalance(new BigDecimal("1000.00"));

        Card toCard = new Card();
        toCard.setId(20L);
        toCard.setUser(user);
        toCard.setStatus(CardStatus.ACTIVE);
        toCard.setBalance(new BigDecimal("500.00"));

        when(currentUserService.getCurrentUserId()).thenReturn(currentUserId);
        when(cardFinder.getByIdAndUserIdOrThrow(10L, currentUserId)).thenReturn(fromCard);
        when(cardFinder.getByIdAndUserIdOrThrow(20L, currentUserId)).thenReturn(toCard);

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> userTransferService.createTransfer(request)
        );

        assertEquals("Недостаточно средств для перевода", exception.getMessage());

        assertEquals(new BigDecimal("1000.00"), fromCard.getBalance());
        assertEquals(new BigDecimal("500.00"), toCard.getBalance());

        verify(transferRepository, never()).save(any(Transfer.class));
    }
}
