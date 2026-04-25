package com.example.bankcards.controller;

import com.example.bankcards.dto.admin.request.CreateCardRequestDTO;
import com.example.bankcards.dto.admin.response.ListCardResponseDTO;
import com.example.bankcards.dto.admin.response.OneCardResponseDTO;
import com.example.bankcards.service.AdminCardService;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/cards")
@Tag(
        name = "Карты администратора",
        description = "Операции администратора по управлению банковскими картами"
)
@Validated
@AllArgsConstructor
public class AdminCardController {

    private final AdminCardService adminCardService;

    @GetMapping
    @Operation(
            summary = "Получить список карт",
            description = "Возвращает список банковских карт. По умолчанию удалённые карты не отображаются"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Список карт успешно получен",
                    content = @Content(schema = @Schema(implementation = ListCardResponseDTO.class))
            ),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован", content = @Content),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав доступа", content = @Content)
    })
    public ResponseEntity<ListCardResponseDTO> getCards(
            @RequestParam(defaultValue = "false") boolean includeDeleted
    ) {
        return new ResponseEntity<>(adminCardService.getCards(includeDeleted), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Получить карту по идентификатору",
            description = "Возвращает данные банковской карты по её идентификатору"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Карта успешно найдена",
                    content = @Content(schema = @Schema(implementation = OneCardResponseDTO.class))
            ),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован", content = @Content),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав доступа", content = @Content),
            @ApiResponse(responseCode = "404", description = "Карта не найдена", content = @Content)
    })
    public ResponseEntity<OneCardResponseDTO> getCardById(@PathVariable long id) {
        return new ResponseEntity<>(adminCardService.getCardById(id), HttpStatus.OK);
    }

    @PostMapping
    @Operation(
            summary = "Создать карту",
            description = "Создаёт новую банковскую карту для пользователя"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Карта успешно создана",
                    content = @Content(schema = @Schema(implementation = OneCardResponseDTO.class))
            ),
            @ApiResponse(responseCode = "400", description = "Некорректные данные запроса", content = @Content),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован", content = @Content),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав доступа", content = @Content),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден", content = @Content),
            @ApiResponse(responseCode = "409", description = "Карта с таким номером уже существует", content = @Content)
    })
    public ResponseEntity<OneCardResponseDTO> createCard(@Valid @RequestBody CreateCardRequestDTO request) {
        return new ResponseEntity<>(adminCardService.createCard(request), HttpStatus.CREATED);
    }

    @PatchMapping("/{id}/block")
    @Operation(
            summary = "Заблокировать карту",
            description = "Переводит банковскую карту в статус BLOCKED"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Карта успешно заблокирована",
                    content = @Content(schema = @Schema(implementation = OneCardResponseDTO.class))
            ),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован", content = @Content),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав доступа", content = @Content),
            @ApiResponse(responseCode = "404", description = "Карта не найдена", content = @Content)
    })
    public ResponseEntity<OneCardResponseDTO> blockCard(@PathVariable long id) {
        return new ResponseEntity<>(adminCardService.blockCard(id), HttpStatus.CREATED);
    }

    @PatchMapping("/{id}/activate")
    @Operation(
            summary = "Активировать карту",
            description = "Переводит банковскую карту в статус ACTIVE"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Карта успешно активирована",
                    content = @Content(schema = @Schema(implementation = OneCardResponseDTO.class))
            ),
            @ApiResponse(responseCode = "400", description = "Карту невозможно активировать", content = @Content),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован", content = @Content),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав доступа", content = @Content),
            @ApiResponse(responseCode = "404", description = "Карта не найдена", content = @Content)
    })
    public ResponseEntity<OneCardResponseDTO> activateCard(@PathVariable long id) {
        return new ResponseEntity<>(adminCardService.activateCard(id), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Удалить карту",
            description = "Логически удаляет банковскую карту через deletedAt"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Карта успешно удалена", content = @Content),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован", content = @Content),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав доступа", content = @Content),
            @ApiResponse(responseCode = "404", description = "Карта не найдена", content = @Content)
    })
    public ResponseEntity<Void> deleteCard(@PathVariable long id) {
        adminCardService.deleteCard(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/block-requests")
    @Operation(
            summary = "Получить заявки на блокировку карт",
            description = "Возвращает список карт, по которым пользователи запросили блокировку"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Список заявок на блокировку успешно получен",
                    content = @Content(schema = @Schema(implementation = ListCardResponseDTO.class))
            ),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован", content = @Content),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав доступа", content = @Content)
    })
    public ResponseEntity<ListCardResponseDTO> getBlockRequestedCards() {
        return ResponseEntity.ok(adminCardService.getBlockRequestedCards());
    }
}