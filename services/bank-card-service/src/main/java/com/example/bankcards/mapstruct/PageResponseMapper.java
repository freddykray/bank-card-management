package com.example.bankcards.mapstruct;

import com.example.bankcards.dto.PageResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
public class PageResponseMapper {

    public <E, D> PageResponseDTO<D> toPageResponse(
            Page<E> page,
            Function<E, D> mapper
    ) {
        return new PageResponseDTO<>(
                page.getContent()
                        .stream()
                        .map(mapper)
                        .toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
    }
}