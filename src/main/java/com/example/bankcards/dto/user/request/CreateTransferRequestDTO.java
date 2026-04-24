package com.example.bankcards.dto.user.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Schema(description = "DTO для создания перевода между картами пользователя")
public class CreateTransferRequestDTO {

    @Schema(name = "from_card_id", description = "Идентификатор карты списания")
    @NotNull
    @Positive
    private Long fromCardId;

    @Schema(name = "to_card_id", description = "Идентификатор карты зачисления")
    @NotNull
    @Positive
    private Long toCardId;

    @Schema(name = "amount", description = "Сумма перевода")
    @NotNull
    @DecimalMin("0.01")
    private BigDecimal amount;
}