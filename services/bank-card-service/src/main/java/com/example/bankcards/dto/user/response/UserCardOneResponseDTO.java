package com.example.bankcards.dto.user.response;

import com.example.bankcards.entity.enums.CardStatus;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Schema(description = "DTO с данными карты пользователя")
public class UserCardOneResponseDTO {

    @Schema(name = "id", description = "Идентификатор карты")
    private Long id;

    @Schema(name = "masked_card_number", description = "Маскированный номер карты")
    private String maskedCardNumber;

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
}