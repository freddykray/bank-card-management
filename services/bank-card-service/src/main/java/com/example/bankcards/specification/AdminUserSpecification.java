package com.example.bankcards.specification;

import com.example.bankcards.dto.admin.request.AdminUserSearchRequestDTO;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.Role;
import com.example.bankcards.entity.enums.UserStatus;
import org.springframework.data.jpa.domain.Specification;

public class AdminUserSpecification {

    public static Specification<User> from(AdminUserSearchRequestDTO request) {
        Specification<User> specification =
                (root, query, cb) -> cb.conjunction();

        if (request.getEmail() != null) {
            specification = specification.and(hasEmail(request.getEmail()));
        }

        if (request.getPhone() != null) {
            specification = specification.and(hasPhone(request.getPhone()));
        }

        if (request.getRole() != null) {
            specification = specification.and(hasRole(request.getRole()));
        }

        if (request.getStatus() != null) {
            specification = specification.and(hasStatus(request.getStatus()));
        }

        if (Boolean.FALSE.equals(request.getIncludeDeleted())) {
            specification = specification.and(notDeleted());
        }

        return specification;
    }

    private static Specification<User> hasEmail(String email) {
        return (root, query, cb) ->
                cb.equal(root.get("email"), email);
    }

    private static Specification<User> hasPhone(String phone) {
        return (root, query, cb) ->
                cb.equal(root.get("phone"), phone);
    }

    private static Specification<User> hasRole(Role role) {
        return (root, query, cb) ->
                cb.equal(root.get("role"), role);
    }

    private static Specification<User> hasStatus(UserStatus status) {
        return (root, query, cb) ->
                cb.equal(root.get("status"), status);
    }

    private static Specification<User> notDeleted() {
        return (root, query, cb) ->
                cb.isNull(root.get("deletedAt"));
    }
}