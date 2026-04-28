package com.example.bankcards.service;

import com.example.bankcards.dto.PageResponseDTO;
import com.example.bankcards.dto.user.request.CreateTransferRequestDTO;
import com.example.bankcards.dto.user.request.UserTransferSearchRequestDTO;
import com.example.bankcards.dto.user.response.OneTransferResponseDTO;

public interface UserTransferService {

    OneTransferResponseDTO createTransfer(CreateTransferRequestDTO request);

    PageResponseDTO<OneTransferResponseDTO> getMyTransfers(UserTransferSearchRequestDTO request);

    OneTransferResponseDTO getMyTransferById(long id);
}