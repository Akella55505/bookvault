package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.dto.RegistrationRequestDTO;
import com.epam.rd.autocode.spring.project.model.Client;
import com.epam.rd.autocode.spring.project.model.User;
import com.epam.rd.autocode.spring.project.repo.ClientRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService")
public class AuthServiceTest {
    @Mock
    private JwtService jwtService;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private ClientRepository clientRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private static final String EMAIL = "user@example.com";
    private static final String PASSWORD = "Secret1!";
    private static final String ENCODED = "encoded-password";
    private static final String NAME = "John Pork";
    private static final String JWT = "header.payload.sig";

    @Nested
    @DisplayName("login()")
    class Login {

        @Test
        @DisplayName("delegates to AuthenticationManager with correct credentials")
        void delegatesAuthentication() {
            Authentication auth = mock(Authentication.class);
            User user = mock(User.class);
            when(auth.getPrincipal()).thenReturn(user);
            when(authenticationManager.authenticate(any())).thenReturn(auth);
            when(jwtService.generateToken(user)).thenReturn(JWT);

            authService.login(EMAIL, PASSWORD);

            ArgumentCaptor<UsernamePasswordAuthenticationToken> captor =
                    ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
            verify(authenticationManager).authenticate(captor.capture());

            assertThat(captor.getValue().getPrincipal()).isEqualTo(EMAIL);
            assertThat(captor.getValue().getCredentials()).isEqualTo(PASSWORD);
        }

        @Test
        @DisplayName("returns JWT generated from the authenticated principal")
        void returnsJwt() {
            Authentication auth = mock(Authentication.class);
            User user = mock(User.class);
            when(auth.getPrincipal()).thenReturn(user);
            when(authenticationManager.authenticate(any())).thenReturn(auth);
            when(jwtService.generateToken(user)).thenReturn(JWT);

            String result = authService.login(EMAIL, PASSWORD);

            assertThat(result).isEqualTo(JWT);
            verify(jwtService).generateToken(user);
        }

        @Test
        @DisplayName("propagates exception when AuthenticationManager rejects credentials")
        void badCredentialsPropagate() {
            when(authenticationManager.authenticate(any()))
                    .thenThrow(new BadCredentialsException("Bad credentials"));

            assertThatThrownBy(() -> authService.login(EMAIL, PASSWORD))
                    .isInstanceOf(BadCredentialsException.class);

            verifyNoInteractions(jwtService);
        }
    }

    @Nested
    @DisplayName("register()")
    class Register {
        private RegistrationRequestDTO dto() {
            return new RegistrationRequestDTO(EMAIL, PASSWORD, NAME);
        }

        @Test
        @DisplayName("saves Client with encoded password, correct email and name")
        void savesClientCorrectly() {
            Client saved = Client.builder().name(NAME).email(EMAIL).password(ENCODED).build();
            when(passwordEncoder.encode(PASSWORD)).thenReturn(ENCODED);
            when(clientRepository.save(any(Client.class))).thenReturn(saved);
            when(jwtService.generateToken(saved)).thenReturn(JWT);

            authService.register(dto());

            ArgumentCaptor<Client> captor = ArgumentCaptor.forClass(Client.class);
            verify(clientRepository).save(captor.capture());
            Client persisted = captor.getValue();

            assertThat(persisted.getEmail()).isEqualTo(EMAIL);
            assertThat(persisted.getName()).isEqualTo(NAME);
            assertThat(persisted.getPassword()).isEqualTo(ENCODED);
        }

        @Test
        @DisplayName("never stores the raw password")
        void passwordIsEncoded() {
            Client saved = Client.builder().name(NAME).email(EMAIL).password(ENCODED).build();
            when(passwordEncoder.encode(PASSWORD)).thenReturn(ENCODED);
            when(clientRepository.save(any(Client.class))).thenReturn(saved);
            when(jwtService.generateToken(saved)).thenReturn(JWT);

            authService.register(dto());

            ArgumentCaptor<Client> captor = ArgumentCaptor.forClass(Client.class);
            verify(clientRepository).save(captor.capture());

            assertThat(captor.getValue().getPassword())
                    .isNotEqualTo(PASSWORD)
                    .isEqualTo(ENCODED);
        }

        @Test
        @DisplayName("returns JWT generated from the saved Client")
        void returnsJwt() {
            Client saved = Client.builder().name(NAME).email(EMAIL).password(ENCODED).build();
            when(passwordEncoder.encode(PASSWORD)).thenReturn(ENCODED);
            when(clientRepository.save(any(Client.class))).thenReturn(saved);
            when(jwtService.generateToken(saved)).thenReturn(JWT);

            String result = authService.register(dto());

            assertThat(result).isEqualTo(JWT);
            verify(jwtService).generateToken(saved);
        }

        @Test
        @DisplayName("generates token from repository-returned instance, not the built one")
        void tokenFromSavedInstance() {
            Client saved = Client.builder().name(NAME).email(EMAIL).password(ENCODED).build();
            when(passwordEncoder.encode(PASSWORD)).thenReturn(ENCODED);
            when(clientRepository.save(any(Client.class))).thenReturn(saved);
            when(jwtService.generateToken(saved)).thenReturn(JWT);

            authService.register(dto());

            verify(jwtService).generateToken(saved);
        }

        @Test
        @DisplayName("propagates exception when repository save fails")
        void repositoryFailurePropagates() {
            when(passwordEncoder.encode(PASSWORD)).thenReturn(ENCODED);
            when(clientRepository.save(any(Client.class)))
                    .thenThrow(new RuntimeException("DB error"));

            assertThatThrownBy(() -> authService.register(dto()))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("DB error");

            verifyNoInteractions(jwtService);
        }
    }
}
