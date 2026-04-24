package com.example.bankcards.dto.admin.response;

import com.example.bankcards.entity.enums.CardStatus;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Schema(description = "DTO с данными банковской карты для администратора")
public class AdminCardResponseDTO {

    @Schema(name = "id", description = "Идентификатор карты")
    private Long id;

    @Schema(name = "masked_card_number", description = "Маскированный номер карты")
    private String maskedCardNumber;

    @Schema(name = "card_number_last4", description = "Последние четыре цифры номера карты")
    private String cardNumberLast4;

    @Schema(name = "owner_name", description = "Имя владельца карты")
    private String ownerName;

    @Schema(name = "expiration_date", description = "Дата окончания срока действия карты")
    private LocalDate expirationDate;

    @Schema(name = "status", description = "Статус карты")
    private CardStatus status;

    @Schema(name = "balance", description = "Баланс карты")
    private BigDecimal balance;

    @Schema(name = "block_requested", description = "Признак запроса на блокировку карты")
    private boolean blockRequested;

    @Schema(name = "block_requested_at", description = "Дата запроса на блокировку карты")
    private Instant blockRequestedAt;

    @Schema(name = "user_id", description = "Идентификатор владельца карты")
    private Long userId;

    @Schema(name = "user_email", description = "Электронная почта владельца карты")
    private String userEmail;

    @Schema(name = "created_at", description = "Дата создания карты")
    private Instant createdAt;

    @Schema(name = "updated_at", description = "Дата последнего обновления карты")
    private Instant updatedAt;
}