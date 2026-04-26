package com.example.bankcards.service;

import com.example.bankcards.dto.PageResponseDTO;
import com.example.bankcards.dto.admin.request.AdminCardSearchRequestDTO;
import com.example.bankcards.dto.admin.request.CreateCardRequestDTO;
import com.example.bankcards.dto.admin.response.ListCardResponseDTO;
import com.example.bankcards.dto.admin.response.OneCardResponseDTO;

public interface AdminCardService {
    PageResponseDTO<OneCardResponseDTO> getCards(AdminCardSearchRequestDTO request);
    OneCardResponseDTO getCardById(long id);
    OneCardResponseDTO createCard(CreateCardRequestDTO request);
    OneCardResponseDTO blockCard(long id);
    OneCardResponseDTO activateCard(long id);
    void deleteCard(long id);
}