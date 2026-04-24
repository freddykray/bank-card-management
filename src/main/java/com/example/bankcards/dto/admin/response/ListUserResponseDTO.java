package com.example.bankcards.dto.admin.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Schema(description = "DTO со списком пользователей")
public class ListUserResponseDTO {

    @Schema(name = "users", description = "Список пользователей")
    private List<OneUserResponseDTO> users;

    @Schema(name = "count", description = "Количество возвращённых пользователей")
    private int count;
}