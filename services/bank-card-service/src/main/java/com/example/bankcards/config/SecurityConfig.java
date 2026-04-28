package com.example.bankcards.config;

import com.example.bankcards.security.CustomUserDetailsService;
import com.example.bankcards.security.JwtFilter;
import com.example.bankcards.security.handler.JwtAccessDeniedHandler;
import com.example.bankcards.security.handler.JwtAuthenticationEntryPoint;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Основная конфигурация Spring Security.
 *
 * <p>Класс настраивает правила доступа к endpoint'ам, JWT-фильтр,
 * stateless-режим работы приложения, обработчики ошибок авторизации
 * и провайдер аутентификации пользователей.</p>
 *
 * <p>Приложение использует JWT-аутентификацию, поэтому HTTP-сессии
 * не создаются и не хранятся на сервере.</p>
 */
@Configuration
@AllArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    private final CustomUserDetailsService customUserDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final UrlBasedCorsConfigurationSource corsConfigurationSource;

    /**
     * Настраивает цепочку фильтров безопасности.
     *
     * <p>В конфигурации включается CORS, отключается CSRF, устанавливается
     * stateless-режим сессий, подключаются обработчики ошибок авторизации
     * и задаются правила доступа к endpoint'ам.</p>
     *
     * <p>JWT-фильтр добавляется перед стандартным
     * {@link UsernamePasswordAuthenticationFilter}, чтобы пользователь
     * мог быть аутентифицирован по Bearer-токену до выполнения основных
     * security-проверок.</p>
     *
     * @param http объект настройки HTTP security
     * @return настроенная цепочка security-фильтров
     * @throws Exception если произошла ошибка при настройке Spring Security
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(jwtAccessDeniedHandler)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/login").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/cards/my/**", "/api/transfers/**").hasAnyRole("USER", "ADMIN")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    /**
     * Создаёт provider для аутентификации пользователей через базу данных.
     *
     * <p>{@link DaoAuthenticationProvider} использует
     * {@link CustomUserDetailsService} для загрузки пользователя по email
     * и {@link PasswordEncoder} для проверки пароля.</p>
     *
     * @return provider аутентификации
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    /**
     * Возвращает {@link AuthenticationManager}, используемый при login-запросе.
     *
     * <p>Через этот manager сервис аутентификации проверяет email и пароль
     * пользователя перед выдачей JWT-токенов.</p>
     *
     * @param configuration конфигурация аутентификации Spring Security
     * @return authentication manager
     * @throws Exception если Spring Security не смог создать AuthenticationManager
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}