package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.model.PasswordChangeToken;
import com.epam.rd.autocode.spring.project.model.User;
import com.epam.rd.autocode.spring.project.repo.PasswordChangeTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PasswordService")
public class PasswordServiceTest {
    @Mock
    private PasswordChangeTokenRepository passwordChangeTokenRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;

    @InjectMocks
    private PasswordService passwordService;

    @Test
    @DisplayName("checks if raw password matches encoded")
    void checksPasswordMatchesEncoded() {
        String raw = "password";
        String encoded = "encodedPassword";

        when(passwordEncoder.matches(eq(raw), eq(encoded))).thenReturn(true);

        assertTrue(passwordService.checkPassword(raw, encoded));
    }

    @Nested
    @DisplayName("create password change token and save it to the repository")
    class CreateToken {
        private static final String TOKEN = "token";
        private static final User USER = mock(User.class);

        @BeforeEach
        void setUp() {
            when(jwtService.generateToken(USER)).thenReturn(TOKEN);
            when(passwordChangeTokenRepository.save(any())).thenReturn(null);
            when(USER.getEmail()).thenReturn("email");
        }

        @Test
        @DisplayName("created new password change token")
        void createsNewToken() {
            when(passwordChangeTokenRepository.findByUserEmail(USER.getEmail())).thenReturn(Optional.empty());

            String receivedToken = passwordService.createAndSavePasswordChangeToken(USER, "password");

            assertEquals(TOKEN, receivedToken);
            verify(passwordChangeTokenRepository).save(any());
            verify(passwordChangeTokenRepository, times(0)).delete(any());
        }

        @Test
        @DisplayName("replaces already existing token")
        void replacesExistingToken() {
            when(passwordChangeTokenRepository.findByUserEmail(USER.getEmail())).thenReturn(Optional.of(new PasswordChangeToken()));
            doNothing().when(passwordChangeTokenRepository).delete(any());

            passwordService.createAndSavePasswordChangeToken(USER, "password");

            verify(passwordChangeTokenRepository).delete(any());
        }
    }
}
