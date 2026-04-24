package com.example.bankcards.dto.user.response;

import com.example.bankcards.entity.enums.TransferStatus;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Schema(description = "DTO с данными перевода пользователя")
public class TransferResponseDTO {

    @Schema(name = "id", description = "Идентификатор перевода")
    private Long id;

    @Schema(name = "from_card_id", description = "Идентификатор карты списания")
    private Long fromCardId;

    @Schema(name = "to_card_id", description = "Идентификатор карты зачисления")
    private Long toCardId;

    @Schema(name = "from_masked_card_number", description = "Маскированный номер карты списания")
    private String fromMaskedCardNumber;

    @Schema(name = "to_masked_card_number", description = "Маскированный номер карты зачисления")
    private String toMaskedCardNumber;

    @Schema(name = "amount", description = "Сумма перевода")
    private BigDecimal amount;

    @Schema(name = "status", description = "Статус перевода")
    private TransferStatus status;

    @Schema(name = "created_at", description = "Дата создания перевода")
    private Instant createdAt;
}