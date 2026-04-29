package com.example.transaction_service.service.impl;

import com.example.transaction_service.entity.CardBalance;
import com.example.transaction_service.entity.ProcessedEvent;
import com.example.transaction_service.model.CreateCardBalanceModel;
import com.example.transaction_service.repository.CardBalanceRepository;
import com.example.transaction_service.repository.ProcessedEventRepository;
import com.example.transaction_service.service.BalanceEventProducer;
import com.example.transaction_service.service.CardBalanceEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class CardBalanceEventServiceImpl implements CardBalanceEventService {

    private static final String CARD_CREATED_EVENT_TYPE = "CARD_CREATED";

    private final CardBalanceRepository cardBalanceRepository;
    private final ProcessedEventRepository processedEventRepository;
    private final BalanceEventProducer balanceEventProducer;

    @Override
    @Transactional
    public void createBalanceFromCardCreatedEvent(CreateCardBalanceModel model) {
        if (processedEventRepository.existsByEventId(model.eventId())) {
            log.info("Событие уже обработано: eventId={}", model.eventId());
            return;
        }

        if (cardBalanceRepository.existsByCardId(model.cardId())) {
            log.info("Баланс для карты уже существует: cardId={}", model.cardId());
            saveProcessedEvent(model);
            return;
        }

        CardBalance cardBalance = buildCardBalance(model);
        CardBalance savedBalance = cardBalanceRepository.save(cardBalance);
        saveProcessedEvent(model);
        balanceEventProducer.sendBalanceChangedEvent(savedBalance);


        log.info(
                "Создан начальный баланс карты: cardId={}, userId={}, balance={}, eventId={}",
                model.cardId(),
                model.userId(),
                model.initialBalance(),
                model.eventId()
        );
    }

    private CardBalance buildCardBalance(CreateCardBalanceModel model) {
        Instant now = Instant.now();

        CardBalance cardBalance = new CardBalance();
        cardBalance.setCardId(model.cardId());
        cardBalance.setUserId(model.userId());
        cardBalance.setBalance(model.initialBalance());
        cardBalance.setCreatedAt(now);
        cardBalance.setUpdatedAt(now);

        return cardBalance;
    }

    private void saveProcessedEvent(CreateCardBalanceModel model) {
        ProcessedEvent processedEvent = new ProcessedEvent();
        processedEvent.setEventId(model.eventId());
        processedEvent.setEventType(CARD_CREATED_EVENT_TYPE);
        processedEvent.setProcessedAt(Instant.now());

        processedEventRepository.save(processedEvent);
    }
}