package com.example.bankcards.specification;

import com.example.bankcards.dto.user.request.UserTransferSearchRequestDTO;
import com.example.bankcards.entity.Transfer;
import com.example.bankcards.entity.enums.TransferStatus;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.Instant;

public class UserTransferSpecification {

    public static Specification<Transfer> from(long userId, UserTransferSearchRequestDTO request) {
        Specification<Transfer> specification =
                (root, query, cb) -> cb.conjunction();

        specification = specification.and(belongsToUser(userId));

        if (request.getStatus() != null) {
            specification = specification.and(hasStatus(request.getStatus()));
        }

        if (request.getFromCardLast4() != null) {
            specification = specification.and(hasFromCardLast4(request.getFromCardLast4()));
        }

        if (request.getToCardLast4() != null) {
            specification = specification.and(hasToCardLast4(request.getToCardLast4()));
        }

        if (request.getAmountFrom() != null) {
            specification = specification.and(amountFrom(request.getAmountFrom()));
        }

        if (request.getAmountTo() != null) {
            specification = specification.and(amountTo(request.getAmountTo()));
        }

        if (request.getCreatedFrom() != null) {
            specification = specification.and(createdFrom(request.getCreatedFrom()));
        }

        if (request.getCreatedTo() != null) {
            specification = specification.and(createdTo(request.getCreatedTo()));
        }

        return specification;
    }

    private static Specification<Transfer> belongsToUser(long userId) {
        return (root, query, cb) ->
                cb.or(
                        cb.equal(root.get("fromCard").get("user").get("id"), userId),
                        cb.equal(root.get("toCard").get("user").get("id"), userId)
                );
    }

    private static Specification<Transfer> hasStatus(TransferStatus status) {
        return (root, query, cb) ->
                cb.equal(root.get("status"), status);
    }

    private static Specification<Transfer> hasFromCardLast4(String fromCardLast4) {
        return (root, query, cb) ->
                cb.equal(root.get("fromCard").get("cardNumberLast4"), fromCardLast4);
    }

    private static Specification<Transfer> hasToCardLast4(String toCardLast4) {
        return (root, query, cb) ->
                cb.equal(root.get("toCard").get("cardNumberLast4"), toCardLast4);
    }

    private static Specification<Transfer> amountFrom(BigDecimal amountFrom) {
        return (root, query, cb) ->
                cb.greaterThanOrEqualTo(root.get("amount"), amountFrom);
    }

    private static Specification<Transfer> amountTo(BigDecimal amountTo) {
        return (root, query, cb) ->
                cb.lessThanOrEqualTo(root.get("amount"), amountTo);
    }

    private static Specification<Transfer> createdFrom(Instant createdFrom) {
        return (root, query, cb) ->
                cb.greaterThanOrEqualTo(root.get("createdAt"), createdFrom);
    }

    private static Specification<Transfer> createdTo(Instant createdTo) {
        return (root, query, cb) ->
                cb.lessThanOrEqualTo(root.get("createdAt"), createdTo);
    }
}