package com.example.bankcards.security;

import com.example.bankcards.exception.UnauthorizedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentUserService {

    public CustomUserDetails getCurrentUserDetails() {
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("Пользователь не авторизован");
        }

        Object principal = authentication.getPrincipal();

        if (!(principal instanceof CustomUserDetails customUserDetails)) {
            throw new UnauthorizedException("Пользователь не авторизован");
        }

        return customUserDetails;
    }

    public long getCurrentUserId() {
        return getCurrentUserDetails().getId();
    }

    public String getCurrentUserEmail() {
        return getCurrentUserDetails().getUsername();
    }
}