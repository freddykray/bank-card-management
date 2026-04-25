package com.example.bankcards.controller;

import com.example.bankcards.dto.admin.request.CreateUserRequestDTO;
import com.example.bankcards.dto.admin.request.UpdateUserRequestDTO;
import com.example.bankcards.dto.admin.request.UpdateUserRoleRequestDTO;
import com.example.bankcards.dto.admin.response.ListUserResponseDTO;
import com.example.bankcards.dto.admin.response.OneUserResponseDTO;
import com.example.bankcards.service.AdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@Tag(
        name = "Операции администратора",
        description = "Операции администратора по управлению пользователями"
)
@Validated
@AllArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    @Operation(
            summary = "Получить список пользователей",
            description = "Возвращает список всех пользователей системы"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Список пользователей успешно получен",
                    content = @Content(schema = @Schema(implementation = ListUserResponseDTO.class))
            ),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован", content = @Content),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав доступа", content = @Content)
    })
    public ResponseEntity<ListUserResponseDTO> getList() {
        return new ResponseEntity<>(adminUserService.getUsers(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Получить пользователя по идентификатору",
            description = "Возвращает данные пользователя по его идентификатору"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Пользователь успешно найден",
                    content = @Content(schema = @Schema(implementation = OneUserResponseDTO.class))
            ),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован", content = @Content),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав доступа", content = @Content),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден", content = @Content)
    })
    public ResponseEntity<OneUserResponseDTO> getUserById(@PathVariable long id) {
        return new ResponseEntity<>(adminUserService.getUserById(id), HttpStatus.OK);
    }

    @PostMapping
    @Operation(
            summary = "Создать пользователя",
            description = "Создаёт нового пользователя системы"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Пользователь успешно создан",
                    content = @Content(schema = @Schema(implementation = OneUserResponseDTO.class))
            ),
            @ApiResponse(responseCode = "400", description = "Некорректные данные запроса", content = @Content),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован", content = @Content),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав доступа", content = @Content),
            @ApiResponse(responseCode = "409", description = "Пользователь с таким email уже существует", content = @Content)
    })
    public ResponseEntity<OneUserResponseDTO> createUser(@Valid @RequestBody CreateUserRequestDTO request) {
        return new ResponseEntity<>(adminUserService.createUser(request), HttpStatus.CREATED);
    }

    @PatchMapping("/{id}")
    @Operation(
            summary = "Изменить данные пользователя",
            description = "Изменяет основные данные пользователя: email, имя, фамилию и телефон"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Данные пользователя успешно изменены",
                    content = @Content(schema = @Schema(implementation = OneUserResponseDTO.class))
            ),
            @ApiResponse(responseCode = "400", description = "Некорректные данные запроса", content = @Content),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован", content = @Content),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав доступа", content = @Content),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден", content = @Content),
            @ApiResponse(responseCode = "409", description = "Пользователь с таким email уже существует", content = @Content)
    })
    public ResponseEntity<OneUserResponseDTO> updateUser(
            @PathVariable long id,
            @Valid @RequestBody UpdateUserRequestDTO request
    ) {
        return new ResponseEntity<>(adminUserService.updateUser(id, request), HttpStatus.OK);
    }

    @PatchMapping("/{id}/role")
    @Operation(
            summary = "Изменить роль пользователя",
            description = "Изменяет роль пользователя в системе"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Роль пользователя успешно изменена",
                    content = @Content(schema = @Schema(implementation = OneUserResponseDTO.class))
            ),
            @ApiResponse(responseCode = "400", description = "Некорректные данные запроса", content = @Content),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован", content = @Content),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав доступа", content = @Content),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден", content = @Content)
    })
    public ResponseEntity<OneUserResponseDTO> updateUserRole(
            @PathVariable long id,
            @Valid @RequestBody UpdateUserRoleRequestDTO request
    ) {
        return new ResponseEntity<>(adminUserService.updateUserRole(id, request), HttpStatus.OK);
    }

    @PatchMapping("/{id}/block")
    @Operation(
            summary = "Заблокировать пользователя",
            description = "Переводит пользователя в статус BLOCKED"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Пользователь успешно заблокирован",
                    content = @Content(schema = @Schema(implementation = OneUserResponseDTO.class))
            ),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован", content = @Content),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав доступа", content = @Content),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден", content = @Content)
    })
    public ResponseEntity<OneUserResponseDTO> blockUser(@PathVariable long id) {
        return new ResponseEntity<>(adminUserService.blockUser(id), HttpStatus.OK);
    }

    @PatchMapping("/{id}/activate")
    @Operation(
            summary = "Активировать пользователя",
            description = "Переводит пользователя в статус ACTIVE"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Пользователь успешно активирован",
                    content = @Content(schema = @Schema(implementation = OneUserResponseDTO.class))
            ),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован", content = @Content),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав доступа", content = @Content),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден", content = @Content)
    })
    public ResponseEntity<OneUserResponseDTO> activateUser(@PathVariable long id) {
        return new ResponseEntity<>(adminUserService.activateUser(id), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Удалить пользователя",
            description = "Логически удаляет пользователя через deletedAt"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Пользователь успешно удалён", content = @Content),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован", content = @Content),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав доступа", content = @Content),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден", content = @Content)
    })
    public ResponseEntity<Void> deleteUser(@PathVariable long id) {
        adminUserService.deleteUser(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}