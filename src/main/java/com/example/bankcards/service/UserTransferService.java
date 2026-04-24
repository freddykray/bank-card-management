package com.example.bankcards.service;

import com.example.bankcards.dto.user.request.CreateTransferRequestDTO;
import com.example.bankcards.dto.user.response.ListTransferResponseDTO;
import com.example.bankcards.dto.user.response.OneTransferResponseDTO;

public interface UserTransferService {

    OneTransferResponseDTO createTransfer(CreateTransferRequestDTO request);

    ListTransferResponseDTO getMyTransfers();

    OneTransferResponseDTO getMyTransferById(long id);
}