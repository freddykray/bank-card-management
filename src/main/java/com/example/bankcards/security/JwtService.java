package com.example.bankcards.security;

import com.example.bankcards.entity.User;
import com.example.bankcards.repository.TokenRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtParserBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.function.Function;

/**
 * Сервис для генерации, чтения и проверки JWT-токенов.
 *
 * <p>Сервис работает с двумя типами токенов:</p>
 *
 * <ul>
 *     <li>access token — используется для доступа к защищённым endpoint'ам;</li>
 *     <li>refresh token — используется для получения новой пары токенов.</li>
 * </ul>
 *
 * <p>Email пользователя сохраняется в subject токена. Подпись токена
 * выполняется секретным ключом из конфигурации приложения.</p>
 */
@Service
@RequiredArgsConstructor
public class JwtService {

    @Value("${app.jwt.secret}")
    private String secretKey;

    @Value("${app.jwt.access.expiration}")
    private long accessTokenExpiration;

    @Value("${app.jwt.refresh.expiration}")
    private long refreshTokenExpiration;

    private final TokenRepository tokenRepository;

    /**
     * Создаёт ключ подписи JWT на основе secret key из конфигурации.
     *
     * @return SecretKey для подписи и проверки JWT
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Генерирует JWT-токен для пользователя.
     *
     * <p>В subject токена записывается email пользователя.
     * Также устанавливаются дата выпуска, дата истечения и подпись.</p>
     *
     * @param user пользователь, для которого создаётся токен
     * @param expiryTime срок жизни токена в миллисекундах
     * @return строковое представление JWT
     */
    private String generateToken(User user, long expiryTime) {
        JwtBuilder builder = Jwts.builder()
                .subject(user.getEmail())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiryTime))
                .signWith(getSigningKey());

        return builder.compact();
    }

    /**
     * Генерирует access token для пользователя.
     *
     * @param user пользователь, для которого создаётся токен
     * @return access token
     */
    public String generateAccessToken(User user) {
        return generateToken(user, accessTokenExpiration);
    }

    /**
     * Генерирует refresh token для пользователя.
     *
     * @param user пользователь, для которого создаётся токен
     * @return refresh token
     */
    public String generateRefreshToken(User user) {
        return generateToken(user, refreshTokenExpiration);
    }

    /**
     * Извлекает все claims из JWT.
     *
     * <p>Перед чтением claims выполняется проверка подписи токена.</p>
     *
     * @param token JWT-токен
     * @return claims токена
     */
    private Claims extractAllClaims(String token) {
        JwtParserBuilder parser = Jwts.parser();
        parser.verifyWith(getSigningKey());

        return parser.build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Извлекает конкретное значение из claims токена.
     *
     * @param token JWT-токен
     * @param resolver функция для получения нужного claim
     * @param <T> тип возвращаемого значения
     * @return значение claim
     */
    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        Claims claims = extractAllClaims(token);
        return resolver.apply(claims);
    }

    /**
     * Извлекает email пользователя из subject токена.
     *
     * @param token JWT-токен
     * @return email пользователя
     */
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Извлекает дату истечения срока действия токена.
     *
     * @param token JWT-токен
     * @return дата истечения токена
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Проверяет, что access token ещё не истёк.
     *
     * @param token JWT-токен
     * @return {@code true}, если срок действия токена ещё не истёк
     */
    private boolean isAccessTokenNotExpired(String token) {
        return !extractExpiration(token).before(new Date());
    }

    /**
     * Проверяет валидность access token.
     *
     * <p>Access token считается валидным, если email из токена совпадает
     * с username из {@link UserDetails}, а срок действия токена ещё не истёк.</p>
     *
     * @param token access token
     * @param user данные пользователя из Spring Security
     * @return {@code true}, если access token валиден
     */
    public boolean isValid(String token, UserDetails user) {
        String email = extractEmail(token);

        return email.equals(user.getUsername())
                && isAccessTokenNotExpired(token);
    }

    /**
     * Проверяет валидность refresh token.
     *
     * <p>Refresh token считается валидным, если:</p>
     *
     * <ul>
     *     <li>email из токена совпадает с email пользователя;</li>
     *     <li>токен не истёк по JWT expiration;</li>
     *     <li>токен существует в базе данных;</li>
     *     <li>токен не был отозван;</li>
     *     <li>срок действия токена в базе данных ещё не истёк.</li>
     * </ul>
     *
     * @param token refresh token
     * @param user пользователь, которому должен принадлежать токен
     * @return {@code true}, если refresh token валиден
     */
    public boolean isValidRefresh(String token, User user) {
        String email = extractEmail(token);

        boolean isStoredTokenValid = tokenRepository.findByRefreshToken(token)
                .map(storedToken ->
                        !storedToken.isLoggedOut()
                                && storedToken.getExpiresAt().isAfter(Instant.now())
                )
                .orElse(false);

        return email.equals(user.getEmail())
                && !isTokenExpired(token)
                && isStoredTokenValid;
    }

    /**
     * Проверяет, истёк ли срок действия JWT.
     *
     * @param token JWT-токен
     * @return {@code true}, если токен истёк
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
}