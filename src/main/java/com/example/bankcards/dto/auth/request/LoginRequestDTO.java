package com.example.bankcards.dto.auth.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Schema(description = "DTO для входа пользователя в систему")
public class LoginRequestDTO {

    @Schema(name = "email", description = "Электронная почта пользователя")
    @Email
    @NotBlank
    private String email;

    @Schema(name = "password", description = "Пароль пользователя")
    @NotBlank
    private String password;
}