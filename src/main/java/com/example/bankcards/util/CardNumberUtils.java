package com.example.bankcards.util;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

@Component
public class CardNumberUtils {

    private static final int CARD_NUMBER_LENGTH = 16;

    private final SecureRandom secureRandom = new SecureRandom();

    public String generate() {
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
}