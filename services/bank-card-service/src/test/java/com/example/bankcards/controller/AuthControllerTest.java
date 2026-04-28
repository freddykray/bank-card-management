package com.example.bankcards.controller;

import com.example.bankcards.dto.auth.request.LoginRequestDTO;
import com.example.bankcards.dto.auth.response.AuthResponseDTO;
import com.example.bankcards.security.JwtFilter;
import com.example.bankcards.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtFilter jwtFilter;

    @Test
    void login_success() throws Exception {
        LoginRequestDTO request = new LoginRequestDTO();
        request.setEmail("admin@example.com");
        request.setPassword("admin123");

        AuthResponseDTO response = new AuthResponseDTO("test-access-token");

        when(authService.authenticate(
                any(LoginRequestDTO.class),
                any(HttpServletResponse.class)
        )).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").value("test-access-token"));

        verify(authService).authenticate(
                any(LoginRequestDTO.class),
                any(HttpServletResponse.class)
        );
    }

    @Test
    void login_invalidRequest_returns400() throws Exception {
        LoginRequestDTO request = new LoginRequestDTO();
        request.setEmail("admin@example.com");
        request.setPassword(null);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).authenticate(
                any(LoginRequestDTO.class),
                any(HttpServletResponse.class)
        );
    }

    @Test
    void refreshToken_success() throws Exception {
        AuthResponseDTO response = new AuthResponseDTO("new-access-token");

        when(authService.refreshToken(
                any(String.class),
                any(HttpServletResponse.class)
        )).thenReturn(response);

        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(new jakarta.servlet.http.Cookie(
                                "refreshToken",
                                "test-refresh-token"
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").value("new-access-token"));

        verify(authService).refreshToken(
                any(String.class),
                any(HttpServletResponse.class)
        );
    }

    @Test
    void refreshToken_withoutCookie_returns401() throws Exception {
        mockMvc.perform(post("/api/auth/refresh"))
                .andExpect(status().isUnauthorized());

        verify(authService, never()).refreshToken(
                any(String.class),
                any(HttpServletResponse.class)
        );
    }
}