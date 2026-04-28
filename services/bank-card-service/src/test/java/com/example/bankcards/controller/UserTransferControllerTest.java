package com.example.bankcards.controller;

import com.example.bankcards.dto.PageResponseDTO;
import com.example.bankcards.dto.user.request.UserTransferSearchRequestDTO;
import com.example.bankcards.security.JwtFilter;
import com.example.bankcards.service.UserTransferService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.example.bankcards.dto.user.request.CreateTransferRequestDTO;
import com.example.bankcards.dto.user.response.OneTransferResponseDTO;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import java.math.BigDecimal;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserTransferController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserTransferControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserTransferService userTransferService;

    @MockitoBean
    private JwtFilter jwtFilter;

    @Test
    void createTransfer_success() throws Exception {
        CreateTransferRequestDTO request = new CreateTransferRequestDTO();
        request.setFromCardId(10L);
        request.setToCardId(20L);
        request.setAmount(new BigDecimal("200.00"));

        OneTransferResponseDTO responseDto = new OneTransferResponseDTO();
        responseDto.setId(100L);

        when(userTransferService.createTransfer(any(CreateTransferRequestDTO.class)))
                .thenReturn(responseDto);

        mockMvc.perform(post("/api/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100L));

        verify(userTransferService).createTransfer(any(CreateTransferRequestDTO.class));
    }

    @Test
    void createTransfer_invalidRequest_returns400() throws Exception {
        CreateTransferRequestDTO request = new CreateTransferRequestDTO();
        request.setFromCardId(10L);
        request.setToCardId(20L);
        request.setAmount(null);

        mockMvc.perform(post("/api/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(userTransferService, never()).createTransfer(any(CreateTransferRequestDTO.class));
    }

    @Test
    void getMyTransfers_success() throws Exception {
        PageResponseDTO<OneTransferResponseDTO> responseDto =
                new PageResponseDTO<>(
                        List.of(),
                        0,
                        10,
                        0,
                        0,
                        true,
                        true
                );

        when(userTransferService.getMyTransfers(any(UserTransferSearchRequestDTO.class)))
                .thenReturn(responseDto);

        mockMvc.perform(get("/api/transfers/my")
                        .param("page", "0")
                        .param("size", "10")
                        .param("status", "SUCCESS")
                        .param("fromCardLast4", "1234")
                        .param("toCardLast4", "5678")
                        .param("amountFrom", "100")
                        .param("amountTo", "5000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.total_elements").value(0))
                .andExpect(jsonPath("$.total_pages").value(0))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(true));

        verify(userTransferService).getMyTransfers(any(UserTransferSearchRequestDTO.class));
    }

    @Test
    void getMyTransferById_success() throws Exception {
        long transferId = 100L;

        OneTransferResponseDTO responseDto = new OneTransferResponseDTO();
        responseDto.setId(transferId);

        when(userTransferService.getMyTransferById(transferId)).thenReturn(responseDto);

        mockMvc.perform(get("/api/transfers/my/{id}", transferId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(transferId));

        verify(userTransferService).getMyTransferById(transferId);
    }
}