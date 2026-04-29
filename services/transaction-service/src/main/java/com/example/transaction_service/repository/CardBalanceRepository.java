package com.example.transaction_service.repository;

import com.example.transaction_service.entity.CardBalance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CardBalanceRepository extends JpaRepository<CardBalance, Long> {

    boolean existsByCardId(Long cardId);

    Optional<CardBalance> findByCardId(Long cardId);
}