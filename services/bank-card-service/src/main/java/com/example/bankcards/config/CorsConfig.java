package com.example.bankcards.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Конфигурация CORS для REST API.
 *
 * <p>CORS-настройки определяют, с каких frontend-доменов разрешено обращаться
 * к backend-приложению, какие HTTP-методы можно использовать и какие заголовки
 * разрешено передавать в запросах.</p>
 *
 * <p>Список разрешённых origins берётся из {@link CorsProperties}, чтобы не
 * хардкодить адреса frontend-приложений в коде и управлять ими через конфигурацию.</p>
 */
@Configuration
@RequiredArgsConstructor
public class CorsConfig {

    private final CorsProperties corsProperties;

    /**
     * Создаёт источник CORS-конфигурации для Spring Security.
     *
     * <p>Конфигурация разрешает основные HTTP-методы REST API,
     * заголовки {@code Authorization} и {@code Content-Type}, а также
     * передачу credentials. Это нужно, например, для работы с JWT-заголовком
     * и cookie refresh token.</p>
     *
     * @return источник CORS-конфигурации для всех endpoint'ов приложения
     */
    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(corsProperties.getAllowedOrigins());

        configuration.setAllowedMethods(List.of(
                "GET",
                "POST",
                "PUT",
                "PATCH",
                "DELETE",
                "OPTIONS"
        ));

        configuration.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type"
        ));

        configuration.setAllowCredentials(true);

        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}