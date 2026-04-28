package com.example.bankcards.service.impl;

import com.example.bankcards.dto.auth.request.LoginRequestDTO;
import com.example.bankcards.dto.auth.response.AuthResponseDTO;
import com.example.bankcards.entity.Token;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.exception.RefreshTokenException;
import com.example.bankcards.repository.TokenRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.JwtService;
import com.example.bankcards.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

/**
 * Реализация сервиса аутентификации.
 *
 * <p>Сервис отвечает за вход пользователя в систему, генерацию access token
 * и refresh token, сохранение refresh token в базе данных, а также обновление
 * access token через refresh token.</p>
 *
 * <p>Access token возвращается клиенту в теле ответа, а refresh token
 * устанавливается в HTTP-only cookie. Такой подход снижает риск доступа
 * к refresh token из JavaScript-кода на клиентской стороне.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final TokenRepository tokenRepository;

    /**
     * Аутентифицирует пользователя по email и паролю.
     *
     * <p>Метод проверяет credentials через {@link AuthenticationManager}.
     * После успешной проверки пользователь загружается из базы данных,
     * для него генерируются access token и refresh token. Refresh token
     * сохраняется в базе данных и устанавливается в HTTP-only cookie.</p>
     *
     * @param request DTO с email и паролем пользователя
     * @param response HTTP-ответ, в который устанавливается cookie с refresh token
     * @return DTO с access token
     * @throws NotFoundException если пользователь не найден после успешной аутентификации
     */
    @Override
    @Transactional
    public AuthResponseDTO authenticate(
            LoginRequestDTO request,
            HttpServletResponse response
    ) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new NotFoundException("User not found"));

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        saveUserToken(accessToken, refreshToken, user);

        addRefreshTokenCookie(response, refreshToken);

        return new AuthResponseDTO(accessToken);
    }

    /**
     * Обновляет access token по refresh token.
     *
     * <p>Метод извлекает email из refresh token, ищет пользователя и проверяет,
     * что refresh token существует в базе данных, не был отозван, не истёк,
     * принадлежит найденному пользователю и является валидным с точки зрения JWT.</p>
     *
     * <p>После успешной проверки старый refresh token помечается как logged out,
     * затем генерируется новая пара access token и refresh token. Новый refresh token
     * сохраняется в базе данных и устанавливается в HTTP-only cookie.</p>
     *
     * @param refreshToken текущий refresh token пользователя
     * @param response HTTP-ответ, в который устанавливается новая cookie с refresh token
     * @return DTO с новым access token
     * @throws NotFoundException если пользователь из refresh token не найден
     * @throws RefreshTokenException если refresh token отсутствует, отозван, истёк,
     *                               принадлежит другому пользователю или невалиден
     */
    @Transactional
    public AuthResponseDTO refreshToken(
            String refreshToken,
            HttpServletResponse response
    ) {
        String email = jwtService.extractEmail(refreshToken);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("No user found"));

        Token storedToken = tokenRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new RefreshTokenException("Refresh token not found"));

        if (storedToken.isLoggedOut()) {
            throw new RefreshTokenException("Refresh token has been revoked");
        }

        if (storedToken.getExpiresAt().isBefore(Instant.now())) {
            throw new RefreshTokenException("Refresh token has expired");
        }

        if (!storedToken.getUser().getId().equals(user.getId())) {
            throw new RefreshTokenException("Refresh token does not belong to user");
        }

        if (!jwtService.isValidRefresh(refreshToken, user)) {
            throw new RefreshTokenException("Refresh token is invalid");
        }

        storedToken.setLoggedOut(true);

        String newAccessToken = jwtService.generateAccessToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(user);

        saveUserToken(newAccessToken, newRefreshToken, user);

        addRefreshTokenCookie(response, newRefreshToken);

        return new AuthResponseDTO(newAccessToken);
    }

    /**
     * Добавляет refresh token в HTTP-only cookie.
     *
     * <p>Cookie используется только для endpoint'а обновления токена.
     * В локальной среде {@code secure=false}; для production-среды значение
     * должно быть {@code true}, чтобы cookie передавалась только по HTTPS.</p>
     *
     * @param response HTTP-ответ
     * @param refreshToken refresh token, который нужно установить в cookie
     */
    private void addRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(false) // локально false, на проде true
                .sameSite("Lax")
                .path("/auth/refresh")
                .maxAge(Duration.ofDays(7))
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    /**
     * Сохраняет пару access token и refresh token в базе данных.
     *
     * <p>Refresh token используется для последующего обновления access token.
     * Дата истечения сохраняется на основе expiration claim из refresh token.</p>
     *
     * @param accessToken access token пользователя
     * @param refreshToken refresh token пользователя
     * @param user пользователь, которому принадлежит пара токенов
     */
    private void saveUserToken(String accessToken, String refreshToken, User user) {
        Token token = Token.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .loggedOut(false)
                .expiresAt(jwtService.extractExpiration(refreshToken).toInstant())
                .createdAt(Instant.now())
                .user(user)
                .build();

        tokenRepository.save(token);
    }
}