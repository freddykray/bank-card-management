package com.example.transaction_service.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record BalanceChangedEvent(
        UUID eventId,
        Long cardId,
        Long userId,
        BigDecimal balance,
        Instant changedAt
) {
}