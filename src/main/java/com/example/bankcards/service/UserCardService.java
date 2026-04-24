package com.example.bankcards.service;

import com.example.bankcards.dto.user.response.CardBalanceResponseDTO;
import com.example.bankcards.dto.user.response.UserCardListResponseDTO;
import com.example.bankcards.dto.user.response.UserCardOneResponseDTO;

public interface UserCardService {

    UserCardListResponseDTO getMyCards();

    UserCardOneResponseDTO getMyCardById(long id);

    CardBalanceResponseDTO getMyCardBalance(long id);

    UserCardOneResponseDTO requestCardBlock(long id);
}