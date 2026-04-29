package com.example.transaction_service.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CreateCardBalanceModel(
        UUID eventId,
        Long cardId,
        Long userId,
        BigDecimal initialBalance,
        Instant eventCreatedAt
) {
}