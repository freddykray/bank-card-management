package com.example.bankcards.dto.admin.request;

import com.example.bankcards.entity.enums.Role;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Schema(description = "DTO для создания пользователя")
public class CreateUserRequestDTO {

    @Schema(name = "email", description = "Электронная почта пользователя")
    @Email
    @NotBlank
    private String email;

    @Schema(name = "password", description = "Пароль пользователя")
    @NotBlank
    private String password;

    @Schema(name = "first_name", description = "Имя пользователя")
    @NotBlank
    private String firstName;

    @Schema(name = "last_name", description = "Фамилия пользователя")
    @NotBlank
    private String lastName;

    @Schema(name = "phone", description = "Номер телефона пользователя")
    @NotBlank
    @Pattern(regexp = "^\\+\\d{10,15}$")
    private String phone;

    @Schema(name = "role", description = "Роль пользователя в системе")
    @NotNull
    private Role role;
}