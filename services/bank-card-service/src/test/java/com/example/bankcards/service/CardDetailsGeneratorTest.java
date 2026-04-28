package com.example.bankcards.service;

import com.example.bankcards.entity.GeneratedCardDetails;
import com.example.bankcards.exception.ConflictException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.util.CardDetailsGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CardDetailsGeneratorTest {

    @Mock
    private CardRepository cardRepository;

    @InjectMocks
    private CardDetailsGenerator cardDetailsGenerator;

    @Test
    void generate_success() {
        when(cardRepository.existsByCardNumberHash(any(String.class)))
                .thenReturn(false);

        GeneratedCardDetails result = cardDetailsGenerator.generate();

        assertNotNull(result);
        assertNotNull(result.cardNumber());
        assertNotNull(result.cardNumberHash());
        assertNotNull(result.last4());
        assertNotNull(result.expirationDate());

        assertEquals(16, result.cardNumber().length());
        assertEquals(64, result.cardNumberHash().length());
        assertEquals(4, result.last4().length());
        assertTrue(result.cardNumber().endsWith(result.last4()));

        assertEquals(LocalDate.now().plusYears(5), result.expirationDate());

        verify(cardRepository).existsByCardNumberHash(result.cardNumberHash());
    }

    @Test
    void generate_allAttemptsUsed_throwsConflictException() {
        when(cardRepository.existsByCardNumberHash(any(String.class)))
                .thenReturn(true);

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> cardDetailsGenerator.generate()
        );

        assertEquals("Не удалось сгенерировать уникальный номер карты", exception.getMessage());

        verify(cardRepository, times(10)).existsByCardNumberHash(any(String.class));
    }
}