package com.example.bankcards.service;

import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.Role;
import com.example.bankcards.entity.enums.UserStatus;
import com.example.bankcards.security.CustomUserDetails;
import com.example.bankcards.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();

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
    void isValid_validToken_returnsTrue() {
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
}