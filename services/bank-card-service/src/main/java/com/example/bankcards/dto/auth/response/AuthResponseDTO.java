package com.example.bankcards.dto.auth.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Schema(description = "DTO с результатом входа пользователя в систему")
public record AuthResponseDTO(

        @Schema(name = "access_token", description = "JWT-токен доступа")
        String accessToken
) {
}