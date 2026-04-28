package com.example.bankcards.repository;

import com.example.bankcards.entity.OutboxEvent;
import com.example.bankcards.entity.enums.OutboxEventStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    @Query("""
            select e
            from OutboxEvent e
            where e.status in :statuses
              and (e.nextRetryAt is null or e.nextRetryAt <= :now)
            order by e.createdAt asc
            """)
    List<OutboxEvent> findReadyToPublish(
            @Param("statuses") List<OutboxEventStatus> statuses,
            @Param("now") Instant now,
            Pageable pageable
    );
}