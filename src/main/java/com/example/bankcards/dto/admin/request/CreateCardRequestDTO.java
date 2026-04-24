package com.example.bankcards.dto.admin.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Schema(description = "DTO для создания банковской карты")
public class CreateCardRequestDTO {

    @Schema(name = "user_id", description = "Идентификатор пользователя, которому создаётся карта")
    @NotNull
    @Positive
    private Long userId;

    @Schema(name = "card_number", description = "Полный номер банковской карты")
    @NotBlank
    @Pattern(regexp = "\\d{16}")
    private String cardNumber;

    @Schema(name = "owner_name", description = "Имя владельца карты")
    @NotBlank
    private String ownerName;

    @Schema(name = "expiration_date", description = "Дата окончания срока действия карты")
    @NotNull
    @Future
    private LocalDate expirationDate;

    @Schema(name = "initial_balance", description = "Начальный баланс карты")
    @NotNull
    @DecimalMin("0.00")
    private BigDecimal initialBalance;
}
