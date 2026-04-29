package com.example.transaction_service.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CardCreatedEvent(
        UUID eventId,
        Long cardId,
        Long userId,
        BigDecimal initialBalance,
        Instant createdAt
) {
}