package com.example.bankcards.controller;

import com.example.bankcards.dto.user.response.CardBalanceResponseDTO;
import com.example.bankcards.dto.user.response.UserCardListResponseDTO;
import com.example.bankcards.dto.user.response.UserCardOneResponseDTO;
import com.example.bankcards.security.JwtFilter;
import com.example.bankcards.service.UserCardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserCardController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserCardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserCardService userCardService;

    @MockitoBean
    private JwtFilter jwtFilter;

    @Test
    void getMyCards_success() throws Exception {
        UserCardListResponseDTO responseDto = new UserCardListResponseDTO();

        when(userCardService.getMyCards()).thenReturn(responseDto);

        mockMvc.perform(get("/api/cards/my"))
                .andExpect(status().isOk());

        verify(userCardService).getMyCards();
    }

    @Test
    void getMyCardById_success() throws Exception {
        long cardId = 10L;

        UserCardOneResponseDTO responseDto = new UserCardOneResponseDTO();
        responseDto.setId(cardId);

        when(userCardService.getMyCardById(cardId)).thenReturn(responseDto);

        mockMvc.perform(get("/api/cards/my/{id}", cardId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(cardId));

        verify(userCardService).getMyCardById(cardId);
    }

    @Test
    void getMyCardBalance_success() throws Exception {
        long cardId = 10L;

        CardBalanceResponseDTO responseDto = new CardBalanceResponseDTO();
        responseDto.setCardId(cardId);

        when(userCardService.getMyCardBalance(cardId)).thenReturn(responseDto);

        mockMvc.perform(get("/api/cards/my/{id}/balance", cardId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.card_id").value(cardId));

        verify(userCardService).getMyCardBalance(cardId);
    }

    @Test
    void requestCardBlock_success() throws Exception {
        long cardId = 10L;

        UserCardOneResponseDTO responseDto = new UserCardOneResponseDTO();
        responseDto.setId(cardId);

        when(userCardService.requestCardBlock(cardId)).thenReturn(responseDto);

        mockMvc.perform(patch("/api/cards/my/{id}/block-request", cardId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(cardId));

        verify(userCardService).requestCardBlock(cardId);
    }
}