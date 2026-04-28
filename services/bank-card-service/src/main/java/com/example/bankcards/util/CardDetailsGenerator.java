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

/**
 * Компонент для генерации технических данных банковской карты.
 *
 * <p>Отвечает за генерацию полного номера карты, hash номера карты,
 * последних 4 цифр и срока действия карты.</p>
 *
 * <p>Полный номер карты используется только внутри backend-приложения:
 * после генерации он шифруется перед сохранением. Для проверки уникальности
 * номера используется SHA-256 hash, который хранится в базе данных.</p>
 */
@Component
@RequiredArgsConstructor
public class CardDetailsGenerator {

    private static final int CARD_NUMBER_LENGTH = 16;
    private static final int MAX_GENERATION_ATTEMPTS = 10;
    private static final int CARD_VALID_YEARS = 5;

    private final CardRepository cardRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Генерирует уникальные данные банковской карты.
     *
     * <p>Метод генерирует номер карты, вычисляет его hash и проверяет,
     * что такого hash ещё нет в базе данных. Если hash уже существует,
     * выполняется повторная попытка генерации.</p>
     *
     * <p>Количество попыток ограничено значением {@link #MAX_GENERATION_ATTEMPTS},
     * чтобы избежать бесконечного цикла при невозможности сгенерировать
     * уникальный номер.</p>
     *
     * @return объект с номером карты, hash, последними 4 цифрами и сроком действия
     * @throws ConflictException если не удалось сгенерировать уникальный номер карты
     */
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

    /**
     * Генерирует случайный номер карты заданной длины.
     *
     * <p>Для генерации используется {@link SecureRandom}, так как номер карты
     * относится к чувствительным данным и не должен генерироваться через
     * обычный {@code Random}.</p>
     *
     * @return строка из цифр длиной {@link #CARD_NUMBER_LENGTH}
     */
    private String generateCardNumber() {
        StringBuilder cardNumber = new StringBuilder(CARD_NUMBER_LENGTH);

        for (int i = 0; i < CARD_NUMBER_LENGTH; i++) {
            cardNumber.append(secureRandom.nextInt(10));
        }

        return cardNumber.toString();
    }

    /**
     * Вычисляет SHA-256 hash полного номера карты.
     *
     * <p>Hash используется для проверки уникальности номера карты без поиска
     * по зашифрованному значению. Результат возвращается в hex-формате.</p>
     *
     * @param cardNumber полный номер карты
     * @return SHA-256 hash номера карты в hex-формате
     * @throws IllegalStateException если алгоритм SHA-256 недоступен в JVM
     */
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

    /**
     * Извлекает последние 4 цифры номера карты.
     *
     * <p>Последние 4 цифры используются для формирования маскированного
     * отображения карты, например {@code **** **** **** 1234}.</p>
     *
     * @param cardNumber полный номер карты
     * @return последние 4 цифры номера карты
     */
    public String extractLast4(String cardNumber) {
        return cardNumber.substring(cardNumber.length() - 4);
    }

    /**
     * Рассчитывает срок действия карты.
     *
     * <p>Срок действия устанавливается автоматически относительно текущей даты.</p>
     *
     * @return дата окончания срока действия карты
     */
    private LocalDate calculateExpirationDate() {
        return LocalDate.now().plusYears(CARD_VALID_YEARS);
    }
}