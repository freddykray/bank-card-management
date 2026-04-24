package com.example.bankcards.dto.user.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Schema(description = "DTO со списком переводов пользователя")
public class ListTransferResponseDTO {

    @Schema(name = "transfers", description = "Список переводов")
    private List<OneTransferResponseDTO> transfers;

    @Schema(name = "count", description = "Количество возвращённых переводов")
    private int count;
}
