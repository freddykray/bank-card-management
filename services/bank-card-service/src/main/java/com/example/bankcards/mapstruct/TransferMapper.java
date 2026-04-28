package com.example.bankcards.mapstruct;


import com.example.bankcards.dto.user.response.OneTransferResponseDTO;
import com.example.bankcards.entity.Transfer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TransferMapper {

    @Mapping(target = "fromCardId", source = "fromCard.id")
    @Mapping(target = "toCardId", source = "toCard.id")
    @Mapping(target = "fromMaskedCardNumber", expression = "java(maskCardNumber(transfer.getFromCard().getCardNumberLast4()))")
    @Mapping(target = "toMaskedCardNumber", expression = "java(maskCardNumber(transfer.getToCard().getCardNumberLast4()))")
    OneTransferResponseDTO toTransferResponse(Transfer transfer);

    default String maskCardNumber(String last4) {
        return "**** **** **** " + last4;
    }
}