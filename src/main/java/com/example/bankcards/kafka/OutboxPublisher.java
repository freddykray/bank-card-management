package com.example.bankcards.kafka;

import com.example.bankcards.entity.OutboxEvent;
import com.example.bankcards.entity.enums.OutboxEventStatus;
import com.example.bankcards.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxPublisher {

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Scheduled(fixedDelayString = "${app.outbox.publisher.fixed-delay-ms:5000}")
    @Transactional
    public void publishReadyEvents() {
        List<OutboxEvent> events = outboxEventRepository.findReadyToPublish(
                List.of(OutboxEventStatus.NEW, OutboxEventStatus.FAILED),
                Instant.now(),
                PageRequest.of(0, 100)
        );

        for (OutboxEvent event : events) {
            publishEvent(event);
        }
    }

    private void publishEvent(OutboxEvent event) {
        try {
            kafkaTemplate.send(
                    event.getTopic(),
                    event.getAggregateId(),
                    event.getPayload()
            ).get(5, TimeUnit.SECONDS);

            event.setStatus(OutboxEventStatus.SENT);
            event.setSentAt(Instant.now());
            event.setErrorMessage(null);
            event.setNextRetryAt(null);

            log.info(
                    "Outbox event отправлен в Kafka: eventId={}, type={}, topic={}, aggregateId={}",
                    event.getEventId(),
                    event.getEventType(),
                    event.getTopic(),
                    event.getAggregateId()
            );
        } catch (Exception exception) {
            int nextRetryCount = event.getRetryCount() + 1;
            event.setRetryCount(nextRetryCount);
            event.setErrorMessage(exception.getMessage());

            if (nextRetryCount >= 5) {
                event.setStatus(OutboxEventStatus.DEAD);
                event.setNextRetryAt(null);
                log.error(
                        "Outbox event переведён в DEAD после {} попыток: eventId={}, type={}, topic={}",
                        nextRetryCount,
                        event.getEventId(),
                        event.getEventType(),
                        event.getTopic(),
                        exception
                );
                return;
            }
            event.setStatus(OutboxEventStatus.FAILED);
            event.setNextRetryAt(Instant.now().plusSeconds(5));

            log.warn(
                    "Ошибка отправки outbox event в Kafka. Будет повторная попытка: eventId={}, retryCount={}, nextRetryAt={}",
                    event.getEventId(),
                    nextRetryCount,
                    event.getNextRetryAt(),
                    exception
            );
        }
    }
}