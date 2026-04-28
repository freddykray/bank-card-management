package com.example.bankcards.service;

import com.example.bankcards.entity.Token;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.Role;
import com.example.bankcards.entity.enums.UserStatus;
import com.example.bankcards.repository.TokenRepository;
import com.example.bankcards.security.CustomUserDetails;
import com.example.bankcards.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @Mock
    private TokenRepository tokenRepository;

    @InjectMocks
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(
                jwtService,
                "secretKey",
                "1234567890123456789012345678901234567890123456789012345678901234"
        );

        ReflectionTestUtils.setField(
                jwtService,
                "accessTokenExpiration",
                3600000L
        );

        ReflectionTestUtils.setField(
                jwtService,
                "refreshTokenExpiration",
                604800000L
        );
    }

    private User createUser(String email) {
        User user = new User();
        user.setId(1L);
        user.setEmail(email);
        user.setPassword("encodedPassword");
        user.setRole(Role.USER);
        user.setStatus(UserStatus.ACTIVE);

        return user;
    }

    private Token createToken(String accessToken, String refreshToken, User user) {
        Token token = new Token();
        token.setAccessToken(accessToken);
        token.setRefreshToken(refreshToken);
        token.setLoggedOut(false);
        token.setExpiresAt(Instant.now().plusSeconds(3600));
        token.setUser(user);

        return token;
    }

    @Test
    void generateAccessToken_success() {
        User user = createUser("user@example.com");

        String token = jwtService.generateAccessToken(user);

        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    void extractEmail_success() {
        User user = createUser("user@example.com");

        String token = jwtService.generateAccessToken(user);

        String email = jwtService.extractEmail(token);

        assertEquals("user@example.com", email);
    }

    @Test
    void isValid_validAccessToken_returnsTrue() {
        User user = createUser("user@example.com");

        String token = jwtService.generateAccessToken(user);

        CustomUserDetails userDetails = new CustomUserDetails(user);

        boolean result = jwtService.isValid(token, userDetails);

        assertTrue(result);
    }

    @Test
    void isValid_wrongUser_returnsFalse() {
        User tokenOwner = createUser("user@example.com");
        User anotherUser = createUser("other@example.com");

        String token = jwtService.generateAccessToken(tokenOwner);

        CustomUserDetails anotherUserDetails = new CustomUserDetails(anotherUser);

        boolean result = jwtService.isValid(token, anotherUserDetails);

        assertFalse(result);
    }

    @Test
    void generateRefreshToken_success() {
        User user = createUser("user@example.com");

        String refreshToken = jwtService.generateRefreshToken(user);

        assertNotNull(refreshToken);
        assertFalse(refreshToken.isBlank());
    }

    @Test
    void extractEmail_fromRefreshToken_success() {
        User user = createUser("user@example.com");

        String refreshToken = jwtService.generateRefreshToken(user);

        String email = jwtService.extractEmail(refreshToken);

        assertEquals("user@example.com", email);
    }

    @Test
    void isValidRefresh_validRefreshToken_returnsTrue() {
        User user = createUser("user@example.com");

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        Token storedToken = createToken(accessToken, refreshToken, user);

        when(tokenRepository.findByRefreshToken(refreshToken))
                .thenReturn(Optional.of(storedToken));

        boolean result = jwtService.isValidRefresh(refreshToken, user);

        assertTrue(result);
    }

    @Test
    void isValidRefresh_tokenNotFoundInDatabase_returnsFalse() {
        User user = createUser("user@example.com");

        String refreshToken = jwtService.generateRefreshToken(user);

        when(tokenRepository.findByRefreshToken(refreshToken))
                .thenReturn(Optional.empty());

        boolean result = jwtService.isValidRefresh(refreshToken, user);

        assertFalse(result);
    }

    @Test
    void isValidRefresh_revokedToken_returnsFalse() {
        User user = createUser("user@example.com");

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        Token storedToken = createToken(accessToken, refreshToken, user);
        storedToken.setLoggedOut(true);

        when(tokenRepository.findByRefreshToken(refreshToken))
                .thenReturn(Optional.of(storedToken));

        boolean result = jwtService.isValidRefresh(refreshToken, user);

        assertFalse(result);
    }

    @Test
    void isValidRefresh_expiredStoredToken_returnsFalse() {
        User user = createUser("user@example.com");

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        Token storedToken = createToken(accessToken, refreshToken, user);
        storedToken.setExpiresAt(Instant.now().minusSeconds(60));

        when(tokenRepository.findByRefreshToken(refreshToken))
                .thenReturn(Optional.of(storedToken));

        boolean result = jwtService.isValidRefresh(refreshToken, user);

        assertFalse(result);
    }

    @Test
    void isValidRefresh_wrongUser_returnsFalse() {
        User tokenOwner = createUser("user@example.com");
        User anotherUser = createUser("other@example.com");

        String accessToken = jwtService.generateAccessToken(tokenOwner);
        String refreshToken = jwtService.generateRefreshToken(tokenOwner);

        Token storedToken = createToken(accessToken, refreshToken, tokenOwner);

        when(tokenRepository.findByRefreshToken(refreshToken))
                .thenReturn(Optional.of(storedToken));

        boolean result = jwtService.isValidRefresh(refreshToken, anotherUser);

        assertFalse(result);
    }
}