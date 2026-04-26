package com.example.bankcards.entity;

import java.time.LocalDate;

public record GeneratedCardDetails(String cardNumber,
                                   String cardNumberHash,
                                   String last4,
                                   LocalDate expirationDate) {
}
