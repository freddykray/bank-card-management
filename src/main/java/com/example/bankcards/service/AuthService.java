package com.example.bankcards.service;

import com.example.bankcards.dto.auth.request.LoginRequestDTO;
import com.example.bankcards.dto.auth.response.AuthResponseDTO;

public interface AuthService {

    AuthResponseDTO login(LoginRequestDTO request);
}
