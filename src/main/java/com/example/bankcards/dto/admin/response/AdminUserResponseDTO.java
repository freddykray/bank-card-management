package com.example.bankcards.dto.admin.response;

import com.example.bankcards.entity.enums.Role;
import com.example.bankcards.entity.enums.UserStatus;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Schema(description = "DTO с данными пользователя для администратора")
public class AdminUserResponseDTO {

    @Schema(name = "id", description = "Идентификатор пользователя")
    private Long id;

    @Schema(name = "email", description = "Электронная почта пользователя")
    private String email;

    @Schema(name = "first_name", description = "Имя пользователя")
    private String firstName;

    @Schema(name = "last_name", description = "Фамилия пользователя")
    private String lastName;

    @Schema(name = "phone", description = "Номер телефона пользователя")
    private String phone;

    @Schema(name = "role", description = "Роль пользователя")
    private Role role;

    @Schema(name = "status", description = "Статус пользователя")
    private UserStatus status;

    @Schema(name = "created_at", description = "Дата создания пользователя")
    private Instant createdAt;

    @Schema(name = "updated_at", description = "Дата последнего обновления пользователя")
    private Instant updatedAt;
}