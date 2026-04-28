package com.example.bankcards.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Событие, которое публикуется после создания банковской карты.
 *
 * <p>Событие используется transaction-service для создания начального баланса
 * карты в своей базе данных.</p>
 *
 * @param eventId уникальный идентификатор события
 * @param cardId идентификатор созданной карты
 * @param userId идентификатор пользователя-владельца карты
 * @param initialBalance начальный баланс карты
 * @param createdAt дата и время создания события
 */
public record CardCreatedEvent(
        UUID eventId,
        Long cardId,
        Long userId,
        BigDecimal initialBalance,
        Instant createdAt
) {
}