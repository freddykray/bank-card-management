package com.example.bankcards.controller;

import com.example.bankcards.dto.user.response.CardBalanceResponseDTO;
import com.example.bankcards.dto.user.response.UserCardListResponseDTO;
import com.example.bankcards.dto.user.response.UserCardOneResponseDTO;
import com.example.bankcards.service.UserCardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cards/my")
@Tag(
        name = "Карты пользователя",
        description = "Операции пользователя со своими банковскими картами"
)
@AllArgsConstructor
public class UserCardController {

    private final UserCardService userCardService;

    @GetMapping
    @Operation(
            summary = "Получить список своих карт",
            description = "Возвращает список банковских карт текущего пользователя"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Список карт успешно получен",
                    content = @Content(schema = @Schema(implementation = UserCardListResponseDTO.class))
            ),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован", content = @Content)
    })
    public ResponseEntity<UserCardListResponseDTO> getListCards() {
        return new ResponseEntity<>(userCardService.getMyCards(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Получить свою карту по идентификатору",
            description = "Возвращает данные карты текущего пользователя по её идентификатору"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Карта успешно найдена",
                    content = @Content(schema = @Schema(implementation = UserCardOneResponseDTO.class))
            ),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован", content = @Content),
            @ApiResponse(responseCode = "403", description = "Нет доступа к данной карте", content = @Content),
            @ApiResponse(responseCode = "404", description = "Карта не найдена", content = @Content)
    })
    public ResponseEntity<UserCardOneResponseDTO> getOneCardById(@PathVariable long id) {
        return new ResponseEntity<>(userCardService.getMyCardById(id), HttpStatus.OK);
    }

    @GetMapping("/{id}/balance")
    @Operation(
            summary = "Получить баланс карты",
            description = "Возвращает баланс карты текущего пользователя"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Баланс карты успешно получен",
                    content = @Content(schema = @Schema(implementation = CardBalanceResponseDTO.class))
            ),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован", content = @Content),
            @ApiResponse(responseCode = "403", description = "Нет доступа к данной карте", content = @Content),
            @ApiResponse(responseCode = "404", description = "Карта не найдена", content = @Content)
    })
    public ResponseEntity<CardBalanceResponseDTO> getOneCardBalance(@PathVariable long id) {
        return new ResponseEntity<>(userCardService.getMyCardBalance(id), HttpStatus.OK);
    }

    @PostMapping("/{id}/block-request")
    @Operation(
            summary = "Запросить блокировку карты",
            description = "Создаёт запрос на блокировку карты текущего пользователя"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Запрос на блокировку карты успешно создан",
                    content = @Content(schema = @Schema(implementation = UserCardOneResponseDTO.class))
            ),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован", content = @Content),
            @ApiResponse(responseCode = "403", description = "Нет доступа к данной карте", content = @Content),
            @ApiResponse(responseCode = "404", description = "Карта не найдена", content = @Content),
            @ApiResponse(responseCode = "409", description = "Запрос на блокировку уже существует", content = @Content)
    })
    public ResponseEntity<UserCardOneResponseDTO> requestCardBlock(@PathVariable long id) {
        return new ResponseEntity<>(userCardService.requestCardBlock(id), HttpStatus.OK);
    }
}