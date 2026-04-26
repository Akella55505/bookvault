package com.epam.rd.autocode.spring.project.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("BruteForceProtectionService")
public class BruteForceProtectionServiceTest {
    private BruteForceProtectionService bruteForceProtectionService;

    private static final String KEY = "key";
    private static int MAX_ATTEMPTS;

    @BeforeEach
    public void setUp() {
        bruteForceProtectionService = new BruteForceProtectionService();
        MAX_ATTEMPTS = bruteForceProtectionService.getMaxAttempts();
    }

    @Test
    @DisplayName("removes key from lockCache after a successful login")
    void removesKeyFromLockCache() {
        bruteForceProtectionService.loginFailed(KEY);
        bruteForceProtectionService.loginSucceeded(KEY);

        for (int i = 0; i < MAX_ATTEMPTS - 1; i++) {
            bruteForceProtectionService.loginFailed(KEY);
        }

        assertFalse(bruteForceProtectionService.isBlocked(KEY));
    }

    @Test
    @DisplayName("adds key to lockCache after exceeding amount of login attemps")
    void blocksKey() {
        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            bruteForceProtectionService.loginFailed(KEY);
        }

        assertTrue(bruteForceProtectionService.isBlocked(KEY));
    }
}
