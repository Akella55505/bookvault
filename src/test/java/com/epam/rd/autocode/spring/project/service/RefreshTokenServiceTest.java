package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.model.RefreshToken;
import com.epam.rd.autocode.spring.project.repo.RefreshTokenRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RefreshTokenService")
public class RefreshTokenServiceTest {
    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    private RefreshTokenService refreshTokenService;

    @BeforeEach
    void setUp() {
        CookieService cookieService = new CookieService();
        refreshTokenService = new RefreshTokenService(refreshTokenRepository, cookieService);
    }

    private static final RefreshToken TOKEN = new RefreshToken(1L, "email", "token", LocalDateTime.now().minusDays(15));

    @Nested
    @DisplayName("createRefreshToken()")
    class CreateToken {
        @BeforeEach
        void setUp() {
            when(refreshTokenRepository.save(any())).thenReturn(TOKEN);
        }

        @Test
        @DisplayName("creates refresh token")
        public void createsToken() {
            when(refreshTokenRepository.findByUserEmail(anyString())).thenReturn(Optional.empty());

            RefreshToken token = refreshTokenService.createRefreshToken("email");
            assertNotNull(token);

            verify(refreshTokenRepository, times(0)).delete(any());
        }

        @Test
        @DisplayName("deletes token from repository if already exists")
        public void deletesToken() {
            when(refreshTokenRepository.findByUserEmail(anyString())).thenReturn(Optional.of(TOKEN));
            doNothing().when(refreshTokenRepository).delete(any());

            refreshTokenService.createRefreshToken("email");

            verify(refreshTokenRepository).delete(any());
        }
    }

    @Test
    @DisplayName("checks if token is expired")
    public void checksTokenIsExpired() {
        assertTrue(refreshTokenService.isExpired(TOKEN));
    }

    @Test
    @DisplayName("creates cookie from a token")
    public void createsCookie() {
        Cookie cookie = refreshTokenService.createCookie(TOKEN);
        assertEquals("refresh", cookie.getName());
        assertEquals(TOKEN.getToken(), cookie.getValue());
    }

    @Test
    @DisplayName("returns token from repository")
    public void returnsToken() {
        when(refreshTokenRepository.findByToken(TOKEN.getToken())).thenReturn(Optional.of(TOKEN));
        Optional<RefreshToken> token = refreshTokenService.getTokenByToken(TOKEN.getToken());
        assertTrue(token.isPresent());
    }
}
