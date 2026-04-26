package com.example.bankcards.util;

import com.example.bankcards.entity.GeneratedCardDetails;
import com.example.bankcards.exception.ConflictException;
import com.example.bankcards.repository.CardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class CardDetailsGenerator {

    private static final int CARD_NUMBER_LENGTH = 16;
    private static final int MAX_GENERATION_ATTEMPTS = 10;
    private static final int CARD_VALID_YEARS = 5;

    private final CardRepository cardRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    public GeneratedCardDetails generate() {
        for (int attempt = 0; attempt < MAX_GENERATION_ATTEMPTS; attempt++) {
            String cardNumber = generateCardNumber();
            String cardNumberHash = hash(cardNumber);

            if (!cardRepository.existsByCardNumberHash(cardNumberHash)) {
                return new GeneratedCardDetails(
                        cardNumber,
                        cardNumberHash,
                        extractLast4(cardNumber),
                        calculateExpirationDate()
                );
            }
        }
        throw new ConflictException("Не удалось сгенерировать уникальный номер карты");
    }

    private String generateCardNumber() {
        StringBuilder cardNumber = new StringBuilder(CARD_NUMBER_LENGTH);

        for (int i = 0; i < CARD_NUMBER_LENGTH; i++) {
            cardNumber.append(secureRandom.nextInt(10));
        }

        return cardNumber.toString();
    }

    public String hash(String cardNumber) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(cardNumber.getBytes(StandardCharsets.UTF_8));

            StringBuilder result = new StringBuilder();

            for (byte b : hashBytes) {
                result.append(String.format("%02x", b));
            }

            return result.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm is not available", e);
        }
    }

    public String extractLast4(String cardNumber) {
        return cardNumber.substring(cardNumber.length() - 4);
    }

    private LocalDate calculateExpirationDate() {
        return LocalDate.now().plusYears(CARD_VALID_YEARS);
    }
}