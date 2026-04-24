package com.example.bankcards.mapstruct;


import com.example.bankcards.dto.user.response.ListTransferResponseDTO;
import com.example.bankcards.dto.user.response.OneTransferResponseDTO;
import com.example.bankcards.entity.Transfer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TransferMapper {

    @Mapping(target = "fromCardId", source = "fromCard.id")
    @Mapping(target = "toCardId", source = "toCard.id")
    @Mapping(target = "fromMaskedCardNumber", expression = "java(maskCardNumber(transfer.getFromCard().getCardNumberLast4()))")
    @Mapping(target = "toMaskedCardNumber", expression = "java(maskCardNumber(transfer.getToCard().getCardNumberLast4()))")
    OneTransferResponseDTO toTransferResponse(Transfer transfer);

    List<OneTransferResponseDTO> toTransferResponseList(List<Transfer> transfers);

    default ListTransferResponseDTO toTransferListResponse(List<Transfer> transfers) {
        List<OneTransferResponseDTO> responseTransfers = toTransferResponseList(transfers);

        ListTransferResponseDTO response = new ListTransferResponseDTO();
        response.setTransfers(responseTransfers);
        response.setCount(responseTransfers.size());

        return response;
    }

    default String maskCardNumber(String last4) {
        return "**** **** **** " + last4;
    }
}