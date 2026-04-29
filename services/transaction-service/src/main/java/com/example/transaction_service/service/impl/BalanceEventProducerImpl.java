package com.example.transaction_service.service.impl;

import com.example.transaction_service.entity.CardBalance;
import com.example.transaction_service.event.BalanceChangedEvent;
import com.example.transaction_service.service.BalanceEventProducer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class BalanceEventProducerImpl implements BalanceEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.kafka.topics.balance-changed}")
    private String balanceChangedTopic;

    @Override
    public void sendBalanceChangedEvent(CardBalance cardBalance) {
        BalanceChangedEvent event = new BalanceChangedEvent(
                UUID.randomUUID(),
                cardBalance.getCardId(),
                cardBalance.getUserId(),
                cardBalance.getBalance(),
                Instant.now()
        );

        try {
            String payload = objectMapper.writeValueAsString(event);

            kafkaTemplate.send(
                    balanceChangedTopic,
                    cardBalance.getCardId().toString(),
                    payload
            ).get(5, TimeUnit.SECONDS);

        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Не удалось сериализовать BalanceChangedEvent", exception);
        } catch (Exception exception) {
            throw new IllegalStateException("Не удалось отправить BalanceChangedEvent в Kafka", exception);
        }
    }
}