package com.example.bankcards.service;

import com.example.bankcards.event.CardCreatedEvent;

public interface OutboxEventService {

    void saveCardCreatedEvent(CardCreatedEvent event);
}