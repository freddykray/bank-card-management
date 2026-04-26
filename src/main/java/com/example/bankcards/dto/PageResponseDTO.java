package com.example.bankcards.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Schema(description = "DTO для постраничного ответа")
public class PageResponseDTO<T> {

    @Schema(description = "Список элементов текущей страницы")
    private List<T> items;

    @Schema(description = "Номер текущей страницы")
    private int page;

    @Schema(description = "Размер страницы")
    private int size;

    @Schema(description = "Общее количество элементов")
    private long totalElements;

    @Schema(description = "Общее количество страниц")
    private int totalPages;

    @Schema(description = "Является ли текущая страница первой")
    private boolean first;

    @Schema(description = "Является ли текущая страница последней")
    private boolean last;
}