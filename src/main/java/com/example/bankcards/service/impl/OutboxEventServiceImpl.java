package com.example.bankcards.service.impl;

import com.example.bankcards.entity.OutboxEvent;
import com.example.bankcards.entity.enums.OutboxEventStatus;
import com.example.bankcards.event.CardCreatedEvent;
import com.example.bankcards.repository.OutboxEventRepository;
import com.example.bankcards.service.OutboxEventService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class OutboxEventServiceImpl implements OutboxEventService {

    private static final String CARD_CREATED_EVENT_TYPE = "CARD_CREATED";

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    @Value("${app.kafka.topics.card-created}")
    private String cardCreatedTopic;

    @Override
    public void saveCardCreatedEvent(CardCreatedEvent event) {
        OutboxEvent outboxEvent = new OutboxEvent();
        outboxEvent.setEventId(event.eventId());
        outboxEvent.setEventType(CARD_CREATED_EVENT_TYPE);
        outboxEvent.setAggregateId(event.cardId().toString());
        outboxEvent.setTopic(cardCreatedTopic);
        outboxEvent.setPayload(toJson(event));
        outboxEvent.setStatus(OutboxEventStatus.NEW);
        outboxEvent.setCreatedAt(Instant.now());

        outboxEventRepository.save(outboxEvent);
    }

    private String toJson(CardCreatedEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Не удалось сериализовать CardCreatedEvent", exception);
        }
    }
}