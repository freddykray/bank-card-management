package com.example.bankcards.service;

import com.example.bankcards.dto.PageResponseDTO;
import com.example.bankcards.dto.admin.response.OneCardResponseDTO;
import com.example.bankcards.dto.user.request.UserCardSearchRequestDTO;
import com.example.bankcards.dto.user.response.CardBalanceResponseDTO;
import com.example.bankcards.dto.user.response.UserCardListResponseDTO;
import com.example.bankcards.dto.user.response.UserCardOneResponseDTO;

public interface UserCardService {
    PageResponseDTO<UserCardOneResponseDTO> getMyCards(UserCardSearchRequestDTO request);
    UserCardOneResponseDTO getMyCardById(long id);
    CardBalanceResponseDTO getMyCardBalance(long id);
    UserCardOneResponseDTO requestCardBlock(long id);
}