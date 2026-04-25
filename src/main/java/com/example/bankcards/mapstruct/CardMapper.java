package com.example.bankcards.mapstruct;

import com.example.bankcards.dto.admin.response.ListCardResponseDTO;
import com.example.bankcards.dto.admin.response.OneCardResponseDTO;
import com.example.bankcards.dto.user.response.CardBalanceResponseDTO;
import com.example.bankcards.dto.user.response.UserCardListResponseDTO;
import com.example.bankcards.dto.user.response.UserCardOneResponseDTO;
import com.example.bankcards.entity.Card;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

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
    OneCardResponseDTO toAdminCardResponse(Card card);

    List<OneCardResponseDTO> toAdminCardResponseList(List<Card> cards);

    default ListCardResponseDTO toAdminCardListResponse(List<Card> cards) {
        List<OneCardResponseDTO> responseCards = toAdminCardResponseList(cards);

        ListCardResponseDTO response = new ListCardResponseDTO();
        response.setCards(responseCards);
        response.setCount(responseCards.size());

        return response;
    }

    @Mapping(target = "maskedCardNumber", expression = "java(\"**** **** **** \" + card.getCardNumberLast4())")
    UserCardOneResponseDTO toUserCardOneResponse(Card card);

    List<UserCardOneResponseDTO> toUserCardOneResponseList(List<Card> cards);

    default UserCardListResponseDTO toUserCardListResponse(List<Card> cards) {
        List<UserCardOneResponseDTO> responseCards = toUserCardOneResponseList(cards);

        UserCardListResponseDTO response = new UserCardListResponseDTO();
        response.setCards(responseCards);
        response.setCount(responseCards.size());

        return response;
    }

    @Mapping(target = "cardId", source = "id")
    @Mapping(target = "maskedCardNumber", expression = "java(\"**** **** **** \" + card.getCardNumberLast4())")
    CardBalanceResponseDTO toCardBalanceResponse(Card card);

}