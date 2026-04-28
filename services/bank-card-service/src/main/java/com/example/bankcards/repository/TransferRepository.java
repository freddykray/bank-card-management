package com.example.bankcards.repository;

import com.example.bankcards.entity.Transfer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransferRepository extends JpaRepository<Transfer, Long>, JpaSpecificationExecutor<Transfer> {

    @Query("""
            select t from Transfer t
            where t.fromCard.user.id = :userId
               or t.toCard.user.id = :userId
            """)
    List<Transfer> findAllByUserId(long userId);

    @Query("""
            select t from Transfer t
            where t.id = :transferId
              and (
                    t.fromCard.user.id = :userId
                    or t.toCard.user.id = :userId
              )
            """)
    Optional<Transfer> findByIdAndUserId(long transferId, long userId);
}