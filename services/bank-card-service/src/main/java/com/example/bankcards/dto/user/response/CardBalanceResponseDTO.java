package com.example.bankcards.dto.user.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Schema(description = "DTO с балансом карты пользователя")
public class CardBalanceResponseDTO {

    @Schema(name = "card_id", description = "Идентификатор карты")
    private Long cardId;

    @Schema(name = "masked_card_number", description = "Маскированный номер карты")
    private String maskedCardNumber;

    @Schema(name = "balance", description = "Баланс карты")
    private BigDecimal balance;
}