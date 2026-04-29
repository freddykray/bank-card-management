package com.example.bankcards.specification;

import com.example.bankcards.dto.user.request.UserCardSearchRequestDTO;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardBalanceView;
import com.example.bankcards.entity.enums.CardStatus;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;

public class UserCardSpecification {

    public static Specification<Card> from(long userId, UserCardSearchRequestDTO request) {
        Specification<Card> specification =
                (root, query, cb) -> cb.conjunction();

        specification = specification.and(belongsToUser(userId));
        specification = specification.and(notDeleted());

        if (request.getStatus() != null) {
            specification = specification.and(hasStatus(request.getStatus()));
        }

        if (request.getLast4() != null) {
            specification = specification.and(hasLast4(request.getLast4()));
        }

        if (request.getExpirationDateFrom() != null) {
            specification = specification.and(expirationDateFrom(request.getExpirationDateFrom()));
        }

        if (request.getExpirationDateTo() != null) {
            specification = specification.and(expirationDateTo(request.getExpirationDateTo()));
        }

        if (request.getBalanceFrom() != null) {
            specification = specification.and(balanceFrom(request.getBalanceFrom()));
        }

        if (request.getBalanceTo() != null) {
            specification = specification.and(balanceTo(request.getBalanceTo()));
        }

        if (request.getBlockRequested() != null) {
            specification = specification.and(hasBlockRequested(request.getBlockRequested()));
        }

        return specification;
    }

    private static Specification<Card> belongsToUser(long userId) {
        return (root, query, cb) ->
                cb.equal(root.get("user").get("id"), userId);
    }

    private static Specification<Card> notDeleted() {
        return (root, query, cb) ->
                cb.isNull(root.get("deletedAt"));
    }

    private static Specification<Card> hasStatus(CardStatus status) {
        return (root, query, cb) ->
                cb.equal(root.get("status"), status);
    }

    private static Specification<Card> hasLast4(String last4) {
        return (root, query, cb) ->
                cb.equal(root.get("cardNumberLast4"), last4);
    }

    private static Specification<Card> expirationDateFrom(LocalDate expirationDateFrom) {
        return (root, query, cb) ->
                cb.greaterThanOrEqualTo(root.get("expirationDate"), expirationDateFrom);
    }

    private static Specification<Card> expirationDateTo(LocalDate expirationDateTo) {
        return (root, query, cb) ->
                cb.lessThanOrEqualTo(root.get("expirationDate"), expirationDateTo);
    }

    private static Specification<Card> balanceFrom(BigDecimal balanceFrom) {
        return (root, query, cb) -> {
            Join<Card, CardBalanceView> balanceView = root.join("balanceView", JoinType.LEFT);
            return cb.greaterThanOrEqualTo(balanceView.get("balance"), balanceFrom);
        };
    }

    private static Specification<Card> balanceTo(BigDecimal balanceTo) {
        return (root, query, cb) -> {
            Join<Card, CardBalanceView> balanceView = root.join("balanceView", JoinType.LEFT);
            return cb.lessThanOrEqualTo(balanceView.get("balance"), balanceTo);
        };
    }

    private static Specification<Card> hasBlockRequested(Boolean blockRequested) {
        return (root, query, cb) ->
                cb.equal(root.get("blockRequested"), blockRequested);
    }
}