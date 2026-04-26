package com.example.bankcards.controller;

import com.example.bankcards.dto.admin.request.CreateCardRequestDTO;
import com.example.bankcards.dto.admin.response.ListCardResponseDTO;
import com.example.bankcards.dto.admin.response.OneCardResponseDTO;
import com.example.bankcards.security.JwtFilter;
import com.example.bankcards.service.AdminCardService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;


import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@WebMvcTest(AdminCardController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminCardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AdminCardService adminCardService;

    @MockitoBean
    private JwtFilter jwtFilter;

    @Test
    void getCards_success() throws Exception {
        ListCardResponseDTO responseDto = new ListCardResponseDTO();

        when(adminCardService.getCards(false)).thenReturn(responseDto);

        mockMvc.perform(get("/api/admin/cards")
                        .param("includeDeleted", "false"))
                .andExpect(status().isOk());

        verify(adminCardService).getCards(false);
    }

    @Test
    void getCardById_success() throws Exception {
        long cardId = 1L;

        OneCardResponseDTO responseDto = new OneCardResponseDTO();
        responseDto.setId(cardId);

        when(adminCardService.getCardById(cardId)).thenReturn(responseDto);

        mockMvc.perform(get("/api/admin/cards/{id}", cardId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(cardId));

        verify(adminCardService).getCardById(cardId);
    }

    @Test
    void createCard_success() throws Exception {
        CreateCardRequestDTO request = new CreateCardRequestDTO();
        request.setUserId(1L);
        request.setCardNumber("1111222233334444");
        request.setOwnerName("REGULAR USER");
        request.setExpirationDate(LocalDate.of(2030, 12, 31));
        request.setInitialBalance(new BigDecimal("10000.00"));

        OneCardResponseDTO responseDto = new OneCardResponseDTO();
        responseDto.setId(10L);

        when(adminCardService.createCard(any(CreateCardRequestDTO.class)))
                .thenReturn(responseDto);

        mockMvc.perform(post("/api/admin/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10L));

        verify(adminCardService).createCard(any(CreateCardRequestDTO.class));
    }

    @Test
    void createCard_invalidRequest_returns400() throws Exception {
        CreateCardRequestDTO request = new CreateCardRequestDTO();
        request.setUserId(1L);
        request.setCardNumber(null);
        request.setOwnerName("REGULAR USER");
        request.setExpirationDate(LocalDate.of(2030, 12, 31));
        request.setInitialBalance(new BigDecimal("10000.00"));

        mockMvc.perform(post("/api/admin/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(adminCardService, never()).createCard(any(CreateCardRequestDTO.class));
    }

    @Test
    void blockCard_success() throws Exception {
        long cardId = 10L;

        OneCardResponseDTO responseDto = new OneCardResponseDTO();
        responseDto.setId(cardId);

        when(adminCardService.blockCard(cardId)).thenReturn(responseDto);

        mockMvc.perform(patch("/api/admin/cards/{id}/block", cardId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(cardId));

        verify(adminCardService).blockCard(cardId);
    }

    @Test
    void activateCard_success() throws Exception {
        long cardId = 10L;

        OneCardResponseDTO responseDto = new OneCardResponseDTO();
        responseDto.setId(cardId);

        when(adminCardService.activateCard(cardId)).thenReturn(responseDto);

        mockMvc.perform(patch("/api/admin/cards/{id}/activate", cardId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(cardId));

        verify(adminCardService).activateCard(cardId);
    }

    @Test
    void deleteCard_success() throws Exception {
        long cardId = 10L;

        mockMvc.perform(delete("/api/admin/cards/{id}", cardId))
                .andExpect(status().isOk());

        verify(adminCardService).deleteCard(cardId);
    }

    @Test
    void getBlockRequestedCards_success() throws Exception {
        ListCardResponseDTO responseDto = new ListCardResponseDTO();

        when(adminCardService.getBlockRequestedCards()).thenReturn(responseDto);

        mockMvc.perform(get("/api/admin/cards/block-requests"))
                .andExpect(status().isOk());

        verify(adminCardService).getBlockRequestedCards();
    }
}