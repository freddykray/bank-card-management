package com.example.bankcards.service;

import com.example.bankcards.event.BalanceChangedEvent;

public interface CardBalanceViewService {

    void updateFromBalanceChangedEvent(BalanceChangedEvent event);
}