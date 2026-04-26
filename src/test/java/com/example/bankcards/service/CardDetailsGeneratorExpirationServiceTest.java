package com.example.bankcards.service;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.repository.CardRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CardDetailsGeneratorExpirationServiceTest {

    @Mock
    private CardRepository cardRepository;

    @InjectMocks
    private CardExpirationService cardExpirationService;

    @Test
    void expireCards_success() {
        Card card1 = new Card();
        card1.setId(1L);
        card1.setStatus(CardStatus.ACTIVE);

        Card card2 = new Card();
        card2.setId(2L);
        card2.setStatus(CardStatus.BLOCKED);

        List<Card> cards = List.of(card1, card2);

        when(cardRepository.findExpiredCandidates(any(LocalDate.class), eq(CardStatus.EXPIRED)))
                .thenReturn(cards);

        cardExpirationService.expireCards();

        assertEquals(CardStatus.EXPIRED, card1.getStatus());
        assertEquals(CardStatus.EXPIRED, card2.getStatus());
        assertNotNull(card1.getUpdatedAt());
        assertNotNull(card2.getUpdatedAt());

        verify(cardRepository).findExpiredCandidates(any(LocalDate.class), eq(CardStatus.EXPIRED));
    }

    @Test
    void expireCards_noExpiredCards_success() {
        when(cardRepository.findExpiredCandidates(any(LocalDate.class), eq(CardStatus.EXPIRED)))
                .thenReturn(List.of());

        cardExpirationService.expireCards();

        verify(cardRepository).findExpiredCandidates(any(LocalDate.class), eq(CardStatus.EXPIRED));
    }
}
