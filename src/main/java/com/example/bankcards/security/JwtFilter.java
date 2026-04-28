package com.example.bankcards.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT-фильтр, который выполняется один раз на каждый HTTP-запрос.
 *
 * <p>Фильтр проверяет наличие заголовка {@code Authorization} с Bearer-токеном.
 * Если токен присутствует и валиден, фильтр загружает пользователя,
 * создаёт объект аутентификации и сохраняет его в {@link SecurityContextHolder}.</p>
 *
 * <p>После успешной установки аутентификации Spring Security воспринимает
 * пользователя как авторизованного и может применять правила доступа по ролям.</p>
 */
@Component
@Slf4j
@AllArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userService;

    /**
     * Обрабатывает входящий HTTP-запрос и пытается авторизовать пользователя по JWT.
     *
     * <p>Если заголовок {@code Authorization} отсутствует или не начинается с
     * {@code Bearer }, запрос просто передаётся дальше по цепочке фильтров.</p>
     *
     * <p>Если токен найден, из него извлекается email пользователя. Затем пользователь
     * загружается через {@link CustomUserDetailsService}, токен валидируется через
     * {@link JwtService}, после чего в security context устанавливается
     * {@link UsernamePasswordAuthenticationToken}.</p>
     *
     * @param request входящий HTTP-запрос
     * @param response HTTP-ответ
     * @param filterChain цепочка фильтров
     * @throws ServletException если произошла ошибка servlet-обработки
     * @throws IOException если произошла ошибка ввода-вывода
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        String email = jwtService.extractEmail(token);

        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userService.loadUserByUsername(email);

            if (jwtService.isValid(token, userDetails)) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                SecurityContextHolder.getContext().setAuthentication(authToken);
                log.debug("Пользователь авторизован по JWT: {}", email);
            }
        }

        filterChain.doFilter(request, response);
    }
}