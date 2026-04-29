package com.example.bankcards.repository;

import com.example.bankcards.entity.CardBalanceView;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CardBalanceViewRepository extends JpaRepository<CardBalanceView, Long> {

    Optional<CardBalanceView> findByCardId(Long cardId);

    boolean existsByCardId(Long cardId);
}