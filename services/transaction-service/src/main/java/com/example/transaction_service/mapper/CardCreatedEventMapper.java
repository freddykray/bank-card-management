package com.example.transaction_service.mapper;

import com.example.transaction_service.event.CardCreatedEvent;
import com.example.transaction_service.model.CreateCardBalanceModel;
import org.springframework.stereotype.Component;

@Component
public class CardCreatedEventMapper {

    public CreateCardBalanceModel toModel(CardCreatedEvent event) {
        return new CreateCardBalanceModel(
                event.eventId(),
                event.cardId(),
                event.userId(),
                event.initialBalance(),
                event.createdAt()
        );
    }
}