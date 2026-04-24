package com.example.bankcards.dto.admin.request;

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
@Schema(description = "DTO для изменения данных пользователя")
public class UpdateUserRequestDTO {

    @Schema(name = "email", description = "Электронная почта пользователя")
    @Email
    @NotBlank
    private String email;

    @Schema(name = "first_name", description = "Имя пользователя")
    @NotBlank
    private String firstName;

    @Schema(name = "last_name", description = "Фамилия пользователя")
    @NotBlank
    private String lastName;

    @Schema(name = "phone", description = "Номер телефона пользователя")
    @NotBlank
    private String phone;
}