package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.repo.PasswordChangeTokenRepository;
import com.epam.rd.autocode.spring.project.repo.RefreshTokenRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DataCleanupService {
    private final PasswordChangeTokenRepository passwordChangeTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    @Scheduled(cron = "0 */5 * * * *")
    @Transactional
    public void deleteOldPasswordChangeTokens() {
        passwordChangeTokenRepository.deleteByExpirationDateBefore(LocalDateTime.now());
    }

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void deleteOldRefreshTokens() {
        refreshTokenRepository.deleteByExpirationDateBefore(LocalDateTime.now());
    }
}
