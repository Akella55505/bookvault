package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.repo.PasswordChangeTokenRepository;
import com.epam.rd.autocode.spring.project.repo.RefreshTokenRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("DataCleanupService")
public class DataCleanupServiceTest {
    @Mock
    private PasswordChangeTokenRepository passwordChangeTokenRepository;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private DataCleanupService dataCleanupService;

    @Test
    @DisplayName("deletes expired password change tokens")
    void deletesExpiredPasswordChangeTokens() {
        dataCleanupService.deleteOldPasswordChangeTokens();

        ArgumentCaptor<LocalDateTime> captor = ArgumentCaptor.forClass(LocalDateTime.class);

        verify(passwordChangeTokenRepository).deleteByExpirationDateBefore(captor.capture());
        assertThat(captor.getValue()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    @DisplayName("deletes expired refresh tokens")
    void deletesExpiredRefreshTokens() {
        dataCleanupService.deleteOldRefreshTokens();

        ArgumentCaptor<LocalDateTime> captor = ArgumentCaptor.forClass(LocalDateTime.class);

        verify(refreshTokenRepository).deleteByExpirationDateBefore(captor.capture());
        assertThat(captor.getValue()).isBeforeOrEqualTo(LocalDateTime.now());
    }
}
