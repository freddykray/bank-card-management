package com.example.bankcards.service;

import com.example.bankcards.dto.auth.request.LoginRequestDTO;
import com.example.bankcards.dto.auth.response.AuthResponseDTO;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {

    AuthResponseDTO authenticate(LoginRequestDTO request, HttpServletResponse response);
    AuthResponseDTO refreshToken(String refreshToken, HttpServletResponse response);



}
