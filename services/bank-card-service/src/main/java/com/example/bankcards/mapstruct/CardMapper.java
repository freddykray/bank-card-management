package com.example.bankcards.mapstruct;

import com.example.bankcards.dto.admin.response.OneCardResponseDTO;
import com.example.bankcards.dto.user.response.CardBalanceResponseDTO;
import com.example.bankcards.dto.user.response.UserCardListResponseDTO;
import com.example.bankcards.dto.user.response.UserCardOneResponseDTO;
import com.example.bankcards.entity.Card;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.math.BigDecimal;
import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedSourcePolicy = ReportingPolicy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface CardMapper {

    @Mapping(target = "maskedCardNumber", expression = "java(\"**** **** **** \" + card.getCardNumberLast4())")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userEmail", source = "user.email")
    @Mapping(target = "balance", source = "balanceView.balance")
    OneCardResponseDTO toAdminCardResponse(Card card);

    @Mapping(target = "maskedCardNumber", expression = "java(\"**** **** **** \" + card.getCardNumberLast4())")
    @Mapping(target = "balance", source = "balanceView.balance")
    UserCardOneResponseDTO toUserCardOneResponse(Card card);

    @Mapping(target = "cardId", source = "id")
    @Mapping(target = "maskedCardNumber", expression = "java(\"**** **** **** \" + card.getCardNumberLast4())")
    @Mapping(target = "balance", source = "balanceView.balance")
    CardBalanceResponseDTO toCardBalanceResponse(Card card);

    default String mapBalance(BigDecimal balance) {
        if (balance == null) {
            return "0";
        }

        return balance.toPlainString();
    }

}