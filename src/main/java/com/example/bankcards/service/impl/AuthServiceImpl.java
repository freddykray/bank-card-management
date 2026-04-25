package com.example.bankcards.service.impl;

import com.example.bankcards.dto.auth.request.LoginRequestDTO;
import com.example.bankcards.dto.auth.response.AuthResponseDTO;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.UnauthorizedException;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.JwtService;
import com.example.bankcards.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    @Override
    public AuthResponseDTO login(LoginRequestDTO request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Неверный email или пароль"));

        String accessToken = jwtService.generateAccessToken(user);

        log.info("Пользователь успешно авторизован: email={}", user.getEmail());

        return new AuthResponseDTO(accessToken);
    }
}