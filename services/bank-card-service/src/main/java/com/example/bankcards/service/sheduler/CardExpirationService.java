package com.example.bankcards.service.sheduler;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.repository.CardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CardExpirationService {

    private final CardRepository cardRepository;

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void expireCards() {
        LocalDate today = LocalDate.now();

        List<Card> expiredCards = cardRepository
                .findExpiredCandidates(
                        today,
                        CardStatus.EXPIRED
                );

        Instant now = Instant.now();

        for (Card card : expiredCards) {
            card.setStatus(CardStatus.EXPIRED);
            card.setUpdatedAt(now);
        }

        if (!expiredCards.isEmpty()) {
            log.info("Обновлены статусы просроченных карт: count={}", expiredCards.size());
        }
    }
}