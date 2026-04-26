package com.example.bankcards.dto.user.request;

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
@Schema(description = "DTO для поиска и постраничного вывода карт пользователя")
public class UserCardSearchRequestDTO {

    @Schema(description = "Номер страницы", defaultValue = "0")
    private int page = 0;

    @Schema(description = "Размер страницы", defaultValue = "10")
    private int size = 10;

    @Schema(description = "Статус карты")
    private CardStatus status;

    @Schema(description = "Последние 4 цифры номера карты")
    private String last4;

    @Schema(description = "Дата окончания срока действия от")
    private LocalDate expirationDateFrom;

    @Schema(description = "Дата окончания срока действия до")
    private LocalDate expirationDateTo;

    @Schema(description = "Минимальный баланс")
    private BigDecimal balanceFrom;

    @Schema(description = "Максимальный баланс")
    private BigDecimal balanceTo;

    @Schema(description = "Есть ли запрос на блокировку карты")
    private Boolean blockRequested;
}