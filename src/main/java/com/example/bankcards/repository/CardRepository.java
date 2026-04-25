package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.enums.CardStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {

    List<Card> findAllByDeletedAtIsNull();

    List<Card> findAllByUserIdAndDeletedAtIsNull(long userId);

    Optional<Card> findByIdAndUserIdAndDeletedAtIsNull(long cardId, long userId);

    List<Card> findAllByBlockRequestedTrueAndDeletedAtIsNull();
}