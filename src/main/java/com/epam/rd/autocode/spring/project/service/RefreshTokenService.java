package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.model.RefreshToken;
import com.epam.rd.autocode.spring.project.repo.RefreshTokenRepository;
import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    @Value("${spring.application.jwt.refresh.expirationTime}")
    private long expirationTime;

    private final RefreshTokenRepository refreshTokenRepository;
    private final CookieService cookieService;

    public RefreshToken createRefreshToken(String userEmail) {
        var optionalToken = refreshTokenRepository.findByUserEmail(userEmail);
        optionalToken.ifPresent(refreshTokenRepository::delete);

        var token = RefreshToken.builder()
                .userEmail(userEmail)
                .token(UUID.randomUUID().toString())
                .build();
        return refreshTokenRepository.save(token);
    }

    public boolean isExpired(RefreshToken token) {
        return token.getExpirationDate().isBefore(LocalDateTime.now());
    }

    public Cookie createCookie(RefreshToken token) {
        return cookieService.createCookie("refresh", token.getToken(), (int) expirationTime);
    }

    public Optional<RefreshToken> getTokenByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }
}
