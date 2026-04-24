package com.example.bankcards.controller;

import com.example.bankcards.dto.user.request.CreateTransferRequestDTO;
import com.example.bankcards.dto.user.response.ListTransferResponseDTO;
import com.example.bankcards.dto.user.response.OneTransferResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transfers")
@Tag(
        name = "Переводы пользователя",
        description = "Операции пользователя по переводу средств между своими картами"
)
public class UserTransferController {

    @PostMapping
    @Operation(
            summary = "Создать перевод",
            description = "Выполняет перевод средств между картами текущего пользователя"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Перевод успешно выполнен",
                    content = @Content(schema = @Schema(implementation = OneTransferResponseDTO.class))
            ),
            @ApiResponse(responseCode = "400", description = "Некорректные данные запроса", content = @Content),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован", content = @Content),
            @ApiResponse(responseCode = "403", description = "Нет доступа к одной из карт", content = @Content),
            @ApiResponse(responseCode = "404", description = "Карта не найдена", content = @Content),
            @ApiResponse(responseCode = "409", description = "Перевод невозможно выполнить", content = @Content)
    })
    public ResponseEntity<OneTransferResponseDTO> createTransfer(@Valid @RequestBody CreateTransferRequestDTO request) {
        return ResponseEntity.status(201).body(null);
    }

    @GetMapping("/my")
    @Operation(
            summary = "Получить историю переводов",
            description = "Возвращает историю переводов текущего пользователя"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "История переводов успешно получена",
                    content = @Content(schema = @Schema(implementation = ListTransferResponseDTO.class))
            ),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован", content = @Content)
    })
    public ResponseEntity<ListTransferResponseDTO> getListTransfers() {
        return ResponseEntity.ok(null);
    }

    @GetMapping("/my/{id}")
    @Operation(
            summary = "Получить перевод по идентификатору",
            description = "Возвращает данные перевода текущего пользователя по его идентификатору"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Перевод успешно найден",
                    content = @Content(schema = @Schema(implementation = OneTransferResponseDTO.class))
            ),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован", content = @Content),
            @ApiResponse(responseCode = "403", description = "Нет доступа к данному переводу", content = @Content),
            @ApiResponse(responseCode = "404", description = "Перевод не найден", content = @Content)
    })
    public ResponseEntity<OneTransferResponseDTO> getOneTransferById(@PathVariable long id) {
        return ResponseEntity.ok(null);
    }
}