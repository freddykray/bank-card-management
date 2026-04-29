package com.example.transaction_service.service;

import com.example.transaction_service.entity.CardBalance;

public interface BalanceEventProducer {
    void sendBalanceChangedEvent(CardBalance cardBalance);
}
