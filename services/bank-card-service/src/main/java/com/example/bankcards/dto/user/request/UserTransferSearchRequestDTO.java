package com.example.bankcards.dto.user.request;

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
@Schema(description = "DTO для поиска и постраничного вывода переводов пользователя")
public class UserTransferSearchRequestDTO {

    @Schema(description = "Номер страницы", defaultValue = "0")
    private int page = 0;

    @Schema(description = "Размер страницы", defaultValue = "10")
    private int size = 10;

    @Schema(description = "Статус перевода")
    private TransferStatus status;

    @Schema(description = "Последние 4 цифры карты списания")
    private String fromCardLast4;

    @Schema(description = "Последние 4 цифры карты зачисления")
    private String toCardLast4;

    @Schema(description = "Минимальная сумма перевода")
    private BigDecimal amountFrom;

    @Schema(description = "Максимальная сумма перевода")
    private BigDecimal amountTo;

    @Schema(description = "Дата создания перевода от")
    private Instant createdFrom;

    @Schema(description = "Дата создания перевода до")
    private Instant createdTo;
}