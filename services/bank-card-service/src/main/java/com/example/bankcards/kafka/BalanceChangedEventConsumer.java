package com.example.bankcards.kafka;

import com.example.bankcards.event.BalanceChangedEvent;
import com.example.bankcards.service.CardBalanceViewService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class BalanceChangedEventConsumer {

    private final ObjectMapper objectMapper;
    private final CardBalanceViewService cardBalanceViewService;

    @KafkaListener(
            topics = "${app.kafka.topics.balance-changed}",
            groupId = "${spring.kafka.consumer.group-id:bank-card-service}"
    )
    public void consume(String payload) {
        try {
            BalanceChangedEvent event =
                    objectMapper.readValue(payload, BalanceChangedEvent.class);

            cardBalanceViewService.updateFromBalanceChangedEvent(event);

            log.info(
                    "BALANCE_CHANGED event обработан: eventId={}, cardId={}, balance={}",
                    event.eventId(),
                    event.cardId(),
                    event.balance()
            );
        } catch (JsonProcessingException exception) {
            log.error("Не удалось прочитать BALANCE_CHANGED event: payload={}", payload, exception);
            throw new IllegalArgumentException("Некорректный BALANCE_CHANGED event", exception);
        }
    }
}