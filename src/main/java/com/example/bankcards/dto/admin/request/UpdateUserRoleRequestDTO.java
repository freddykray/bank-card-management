package com.example.bankcards.dto.admin.request;

import com.example.bankcards.entity.enums.Role;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Schema(description = "DTO для изменения роли пользователя")
public class UpdateUserRoleRequestDTO {

    @Schema(name = "role", description = "Новая роль пользователя")
    @NotNull
    private Role role;
}