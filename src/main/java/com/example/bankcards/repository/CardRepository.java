package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.enums.CardStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {

    List<Card> findAllByDeletedAtIsNull();

    List<Card> findAllByUserIdAndDeletedAtIsNull(long userId);

    Optional<Card> findByIdAndUserIdAndDeletedAtIsNull(long cardId, long userId);

    List<Card> findAllByBlockRequestedTrueAndDeletedAtIsNull();

    @Query("""
        select c
        from Card c
        where c.expirationDate < :date
          and c.deletedAt is null
          and c.status <> :status
        """)
    List<Card> findExpiredCandidates(
            @Param("date") LocalDate date,
            @Param("status") CardStatus status
    );

    boolean existsByCardNumberHash(String cardNumberHash);
}