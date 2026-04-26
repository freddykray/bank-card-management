package com.example.bankcards.dto.admin.request;

import com.example.bankcards.entity.enums.Role;
import com.example.bankcards.entity.enums.UserStatus;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Schema(description = "DTO для поиска и постраничного вывода пользователей администратором")
public class AdminUserSearchRequestDTO {

    @Schema(description = "Номер страницы", defaultValue = "0")
    private int page = 0;

    @Schema(description = "Размер страницы", defaultValue = "10")
    private int size = 10;

    @Schema(description = "Email пользователя")
    private String email;

    @Schema(description = "Номер телефона пользователя")
    private String phone;

    @Schema(description = "Роль пользователя")
    private Role role;

    @Schema(description = "Статус пользователя")
    private UserStatus status;

    @Schema(description = "Включать ли логически удалённых пользователей", defaultValue = "false")
    private Boolean includeDeleted = false;
}