package com.example.transaction_service.service;

import com.example.transaction_service.event.CardCreatedEvent;
import com.example.transaction_service.mapper.CardCreatedEventMapper;
import com.example.transaction_service.model.CreateCardBalanceModel;
import com.example.transaction_service.service.CardBalanceEventService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CardCreatedEventConsumer {

    private final ObjectMapper objectMapper;
    private final CardCreatedEventMapper cardCreatedEventMapper;
    private final CardBalanceEventService cardBalanceEventService;

    @KafkaListener(
            topics = "${app.kafka.topics.card-created}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consume(String payload) {
        try {
            CardCreatedEvent event = objectMapper.readValue(payload, CardCreatedEvent.class);

            CreateCardBalanceModel model = cardCreatedEventMapper.toModel(event);

            cardBalanceEventService.createBalanceFromCardCreatedEvent(model);

            log.info(
                    "CARD_CREATED event обработан: eventId={}, cardId={}, userId={}",
                    event.eventId(),
                    event.cardId(),
                    event.userId()
            );
        } catch (JsonProcessingException exception) {
            log.error("Не удалось прочитать CARD_CREATED event: payload={}", payload, exception);
            throw new IllegalArgumentException("Некорректный CARD_CREATED event", exception);
        }
    }
}