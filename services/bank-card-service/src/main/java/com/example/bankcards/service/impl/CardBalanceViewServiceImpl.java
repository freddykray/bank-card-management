package com.example.bankcards.service.impl;

import com.example.bankcards.entity.CardBalanceView;
import com.example.bankcards.event.BalanceChangedEvent;
import com.example.bankcards.repository.CardBalanceViewRepository;
import com.example.bankcards.service.CardBalanceViewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class CardBalanceViewServiceImpl implements CardBalanceViewService {

    private final CardBalanceViewRepository cardBalanceViewRepository;

    @Override
    @Transactional
    public void updateFromBalanceChangedEvent(BalanceChangedEvent event) {
        CardBalanceView view = cardBalanceViewRepository.findByCardId(event.cardId())
                .orElseGet(() -> createNewView(event));

        if (isOldEvent(event, view)) {
            log.info(
                    "BALANCE_CHANGED event пропущен как устаревший: cardId={}, eventChangedAt={}, currentBalanceUpdatedAt={}",
                    event.cardId(),
                    event.changedAt(),
                    view.getBalanceUpdatedAt()
            );
            return;
        }

        view.setUserId(event.userId());
        view.setBalance(event.balance());
        view.setBalanceUpdatedAt(event.changedAt());
        view.setReceivedAt(Instant.now());

        cardBalanceViewRepository.save(view);

        log.info(
                "Read model баланса обновлена: cardId={}, userId={}, balance={}",
                event.cardId(),
                event.userId(),
                event.balance()
        );
    }

    private CardBalanceView createNewView(BalanceChangedEvent event) {
        CardBalanceView view = new CardBalanceView();
        view.setCardId(event.cardId());
        return view;
    }

    private boolean isOldEvent(BalanceChangedEvent event, CardBalanceView view) {
        return view.getBalanceUpdatedAt() != null
                && event.changedAt().isBefore(view.getBalanceUpdatedAt());
    }
}