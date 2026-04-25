package com.example.bankcards.service.impl;

import com.example.bankcards.dto.user.request.CreateTransferRequestDTO;
import com.example.bankcards.dto.user.response.ListTransferResponseDTO;
import com.example.bankcards.dto.user.response.OneTransferResponseDTO;
import com.example.bankcards.mapstruct.TransferMapper;
import com.example.bankcards.repository.TransferRepository;
import com.example.bankcards.service.UserTransferService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserTransferServiceImpl implements UserTransferService {

    private final TransferRepository transferRepository;
    private final TransferMapper transferMapper;

    @Override
    public OneTransferResponseDTO createTransfer(CreateTransferRequestDTO request) {

        return null;
    }

    @Override
    public ListTransferResponseDTO getMyTransfers() {
        return null;
    }

    @Override
    public OneTransferResponseDTO getMyTransferById(long id) {
        return null;
    }
}
