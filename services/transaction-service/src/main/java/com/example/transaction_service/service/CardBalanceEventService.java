package com.example.transaction_service.service;

import com.example.transaction_service.model.CreateCardBalanceModel;

public interface CardBalanceEventService {

    void createBalanceFromCardCreatedEvent(CreateCardBalanceModel model);

}