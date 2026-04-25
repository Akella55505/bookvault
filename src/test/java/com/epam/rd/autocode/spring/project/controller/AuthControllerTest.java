package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.RegistrationRequestDTO;
import com.epam.rd.autocode.spring.project.model.RefreshToken;
import com.epam.rd.autocode.spring.project.model.User;
import com.epam.rd.autocode.spring.project.service.*;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Locale;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@DisplayName("AuthController")
@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;
    @MockBean
    private JwtService jwtService;
    @MockBean
    private MessageSource messageSource;
    @MockBean
    private UserService userService;
    @MockBean
    private EmailService emailService;
    @MockBean
    private PasswordEncoder passwordEncoder;
    @MockBean
    private RefreshTokenService refreshTokenService;
    @MockBean
    private BruteForceProtectionService bruteForceProtectionService;
    @MockBean
    private CookieService cookieService;

    private static final String EMAIL = "user@example.com";
    private static final String PASSWORD = "Secret1!";
    private static final String NAME = "John Pork";
    private static final String JWT = "header.payload.sig";
    private static final String TOKEN = "reset.jwt.token";
    private static final String STRING_DATA = "data";
    private static final Locale LOCALE = Locale.ENGLISH;

    private Cookie jwtCookie;
    private Cookie refreshCookie;

    @BeforeEach
    void setUp() {
        jwtCookie = new Cookie("jwt", JWT);
        refreshCookie = new Cookie("refresh", "refresh-token-value");
    }

    @Nested
    @DisplayName("GET /auth/blocked")
    class BlockedPage {

        @Test
        @DisplayName("returns auth/blocked view")
        void returnsView() throws Exception {
            mockMvc
                    .perform(get("/auth/blocked"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("auth/blocked"));
        }
    }

    @Nested
    @DisplayName("GET /auth/login")
    class LoginPage {

        @Test
        @DisplayName("returns auth/login view")
        void returnsView() throws Exception {
            mockMvc
                    .perform(get("/auth/login"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("auth/login"));
        }

        @Test
        @DisplayName("adds returnUrl to model when provided")
        void addsReturnUrl() throws Exception {
            mockMvc
                    .perform(get("/auth/login")
                            .param("returnUrl", "/books"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("auth/login"))
                    .andExpect(model().attribute("returnUrl", "/books"));
        }

        @Test
        @DisplayName("adds null returnUrl when not provided")
        void nullReturnUrl() throws Exception {
            mockMvc
                    .perform(get("/auth/login"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("auth/login"))
                    .andExpect(model().attributeDoesNotExist("returnUrl"));
        }
    }

    @Nested
    @DisplayName("POST /auth/login")
    class Login {

        @Test
        @DisplayName("blocked user is redirected to /auth/blocked")
        void blockedRedirect() throws Exception {
            when(bruteForceProtectionService.isBlocked(EMAIL)).thenReturn(true);

            mockMvc
                    .perform(post("/auth/login")
                            .param("email" , EMAIL)
                            .param("password", PASSWORD))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(view().name("redirect:/auth/blocked"));

            verifyNoInteractions(authService);
        }

        @Test
        @DisplayName("successful login without rememberMe redirects to /")
        void successNoRememberMe() throws Exception {
            when(bruteForceProtectionService.isBlocked(EMAIL)).thenReturn(false);
            when(authService.login(EMAIL, PASSWORD)).thenReturn(JWT);
            when(jwtService.createCookie(JWT)).thenReturn(jwtCookie);

            mockMvc
                    .perform(post("/auth/login")
                            .param("email" , EMAIL)
                            .param("password", PASSWORD))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(view().name("redirect:/"))
                    .andExpect(cookie().value("jwt", JWT));

            verify(bruteForceProtectionService).loginSucceeded(EMAIL);
            verifyNoInteractions(refreshTokenService);
        }

        @Test
        @DisplayName("successful login with returnUrl redirects to returnUrl")
        void successWithReturnUrl() throws Exception {
            when(bruteForceProtectionService.isBlocked(EMAIL)).thenReturn(false);
            when(authService.login(EMAIL, PASSWORD)).thenReturn(JWT);
            when(jwtService.createCookie(JWT)).thenReturn(jwtCookie);

            mockMvc
                    .perform(post("/auth/login")
                            .param("email" , EMAIL)
                            .param("password", PASSWORD)
                            .param("returnUrl", "/books"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(view().name("redirect:/books"));
        }

        @Test
        @DisplayName("successful login with blank returnUrl redirects to /")
        void successBlankReturnUrl() throws Exception {
            when(bruteForceProtectionService.isBlocked(EMAIL)).thenReturn(false);
            when(authService.login(EMAIL, PASSWORD)).thenReturn(JWT);
            when(jwtService.createCookie(JWT)).thenReturn(jwtCookie);

            mockMvc
                    .perform(post("/auth/login")
                            .param("email" , EMAIL)
                            .param("password", PASSWORD))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(view().name("redirect:/"));
        }

        @Test
        @DisplayName("successful login with rememberMe issues refresh cookie")
        void successWithRememberMe() throws Exception {
            RefreshToken token = mock(RefreshToken.class);
            when(bruteForceProtectionService.isBlocked(EMAIL)).thenReturn(false);
            when(authService.login(EMAIL, PASSWORD)).thenReturn(JWT);
            when(jwtService.createCookie(JWT)).thenReturn(jwtCookie);
            when(refreshTokenService.createRefreshToken(EMAIL)).thenReturn(token);
            when(refreshTokenService.createCookie(token)).thenReturn(refreshCookie);

            mockMvc
                    .perform(post("/auth/login")
                            .param("email" , EMAIL)
                            .param("password", PASSWORD)
                            .param("rememberMe", "true"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(view().name("redirect:/"))
                    .andExpect(cookie().value("jwt", JWT))
                    .andExpect(cookie().exists("refresh"));
        }

        @Test
        @DisplayName("bad credentials increments brute-force counter and re-renders login")
        void badCredentials() throws Exception {
            when(bruteForceProtectionService.isBlocked(EMAIL)).thenReturn(false);
            when(authService.login(EMAIL, PASSWORD)).thenThrow(new RuntimeException("bad credentials"));

            mockMvc
                    .perform(post("/auth/login")
                            .param("email", EMAIL)
                            .param("password", PASSWORD))
                    .andExpect(status().isOk())
                    .andExpect(view().name("auth/login"))
                    .andExpect(model().attributeExists("errorMessage"));

            verify(bruteForceProtectionService).loginFailed(EMAIL);
            verify(bruteForceProtectionService, never()).loginSucceeded(any());
        }

        @Test
        @DisplayName("failed login preserves returnUrl in model")
        void badCredentialsPreservesReturnUrl() throws Exception {
            when(bruteForceProtectionService.isBlocked(EMAIL)).thenReturn(false);
            when(authService.login(EMAIL, PASSWORD)).thenThrow(new RuntimeException("Bad credentials"));

            mockMvc
                    .perform(post("/auth/login")
                            .param("email" , EMAIL)
                            .param("password", PASSWORD)
                            .param("returnUrl", "/books"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("auth/login"))
                    .andExpect(model().attributeExists("errorMessage"))
                    .andExpect(model().attribute("returnUrl", "/books"));
        }
    }

    @Nested
    @DisplayName("GET /auth/register")
    class RegisterPage {

        @Test
        @DisplayName("returns auth/register view")
        void returnsView() throws Exception {
            mockMvc
                    .perform(get("/auth/register"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("auth/register"));
        }

        @Test
        @DisplayName("adds returnUrl to model")
        void addsReturnUrl() throws Exception {
            mockMvc
                    .perform(get("/auth/register")
                            .param("returnUrl", "/books"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("auth/register"))
                    .andExpect(model().attribute("returnUrl", "/books"));
        }
    }

    @Nested
    @DisplayName("POST /auth/register")
    class Register {
        private RegistrationRequestDTO dto;

        @BeforeEach
        void setUp() {
            dto = new RegistrationRequestDTO(EMAIL, PASSWORD, NAME);
        }

        @Test
        @DisplayName("validation errors re-render register form with returnUrl")
        void validationErrors() throws Exception {
            dto.setEmail("error");

            mockMvc
                    .perform(post("/auth/register")
                            .flashAttr("registrationRequestDTO", dto)
                            .param("returnUrl", "/books"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("auth/register"))
                    .andExpect(model().attribute("returnUrl", "/books"));

            verifyNoInteractions(authService);
        }

        @Test
        @DisplayName("valid registration redirects to returnUrl")
        void successWithReturnUrl() throws Exception {
            when(authService.register(dto)).thenReturn(JWT);
            when(jwtService.createCookie(JWT)).thenReturn(jwtCookie);

            mockMvc
                    .perform(post("/auth/register")
                            .flashAttr("registrationRequestDTO", dto)
                            .param("returnUrl", "/books"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(view().name("redirect:/books"))
                    .andExpect(cookie().value("jwt", JWT));
        }

        @Test
        @DisplayName("valid registration with null returnUrl redirects to /")
        void successNullReturnUrl() throws Exception {
            when(authService.register(dto)).thenReturn(JWT);
            when(jwtService.createCookie(JWT)).thenReturn(jwtCookie);

            mockMvc
                    .perform(post("/auth/register")
                            .flashAttr("registrationRequestDTO", dto))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(view().name("redirect:/"))
                    .andExpect(cookie().value("jwt", JWT));
        }
    }

    @Nested
    @DisplayName("GET /auth/forgot-password")
    class ForgotPasswordPage {

        @Test
        @DisplayName("returns auth/forgot-password view")
        void returnsView() throws Exception {
            mockMvc
                    .perform(get("/auth/forgot-password"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("auth/forgot-password"));
        }
    }

    @Nested
    @DisplayName("POST /auth/forgot-password")
    class ForgotPasswordSubmit {

        @Test
        @DisplayName("known email triggers email send and sets emailSent=true")
        void knownEmail() throws Exception {
            when(userService.checkUserExistsByEmail(EMAIL)).thenReturn(true);
            when(messageSource.getMessage(eq("forgot.email.subject"), any(), eq(LOCALE)))
                    .thenReturn("Reset your password");
            when(messageSource.getMessage(eq("forgot.email.text"), any(), eq(LOCALE)))
                    .thenReturn("Click the link");
            when(jwtService.generateToken(EMAIL)).thenReturn(TOKEN);

            mockMvc
                    .perform(post("/auth/forgot-password")
                            .param("email", EMAIL))
                    .andExpect(status().isOk())
                    .andExpect(view().name("auth/forgot-password"))
                    .andExpectAll(model().attribute("emailSent", true),
                            model().attribute("submittedEmail", EMAIL));

            verify(emailService).sendVerificationEmail(eq(EMAIL), anyString(), anyString(), contains(TOKEN));
        }

        @Test
        @DisplayName("unknown email still sets emailSent=true (prevents enumeration)")
        void unknownEmail() throws Exception {
            when(userService.checkUserExistsByEmail(EMAIL)).thenReturn(false);

            mockMvc
                    .perform(post("/auth/forgot-password")
                            .param("email", EMAIL))
                    .andExpect(status().isOk())
                    .andExpect(view().name("auth/forgot-password"))
                    .andExpectAll(model().attribute("emailSent", true),
                            model().attribute("submittedEmail", EMAIL));

            verifyNoInteractions(emailService);
            verifyNoInteractions(jwtService);
        }
    }

    @Nested
    @DisplayName("GET /auth/reset-password")
    class ResetPasswordPage {

        @Test
        @DisplayName("expired token sets tokenInvalid=true and omits token from model")
        void expiredToken() throws Exception {
            when(jwtService.isExpired(STRING_DATA)).thenReturn(true);

            mockMvc
                    .perform(get("/auth/reset-password")
                            .param("token", STRING_DATA))
                    .andExpect(status().isOk())
                    .andExpect(view().name("auth/reset-password"))
                    .andExpectAll(model().attribute("tokenInvalid", true),
                            model().attribute("resetSuccess", false),
                            model().attributeDoesNotExist("token"));
        }

        @Test
        @DisplayName("valid token sets tokenInvalid=false and includes token in model")
        void validToken() throws Exception {
            when(jwtService.isExpired(TOKEN)).thenReturn(false);

            mockMvc
                    .perform(get("/auth/reset-password")
                            .param("token", STRING_DATA))
                    .andExpect(status().isOk())
                    .andExpect(view().name("auth/reset-password"))
                    .andExpectAll(model().attribute("tokenInvalid", false),
                            model().attribute("resetSuccess", false),
                            model().attribute("token", STRING_DATA));
        }
    }

    @Nested
    @DisplayName("POST /auth/reset-password")
    class ResetPassword {

        private static final String VALID_PASSWORD = "NewSecret1!";
        private static final String WEAK_PASSWORD = "weak";
        private static final String ENCODED_PASSWORD = "encoded";

        private User user;

        @BeforeEach
        void setUp() {
            user = mock(User.class);
            when(user.getEmail()).thenReturn(EMAIL);
        }

        @Test
        @DisplayName("invalid token sets tokenInvalid=true")
        void invalidToken() throws Exception {
            when(jwtService.extractSubject(TOKEN)).thenThrow(new RuntimeException("Bad token"));

            mockMvc
                    .perform(post("/auth/reset-password")
                            .param("token", TOKEN)
                            .param("newPassword", VALID_PASSWORD)
                            .param("confirmPassword", VALID_PASSWORD))
                    .andExpect(view().name("auth/reset-password"))
                    .andExpectAll(model().attribute("tokenInvalid", true),
                            model().attribute("resetSuccess", false));

            verifyNoInteractions(userService, passwordEncoder);
        }

        @Test
        @DisplayName("isValid throwing sets tokenInvalid=true")
        void tokenFailsValidation() throws Exception {
            when(jwtService.extractSubject(TOKEN)).thenReturn(EMAIL);
            when(userService.findUserByEmail(EMAIL)).thenReturn(user);
            when(jwtService.isValid(TOKEN, user)).thenThrow(new RuntimeException("Expired"));

            mockMvc
                    .perform(post("/auth/reset-password")
                            .param("token", TOKEN)
                            .param("newPassword", VALID_PASSWORD)
                            .param("confirmPassword", VALID_PASSWORD))
                    .andExpect(view().name("auth/reset-password"))
                    .andExpectAll(model().attribute("tokenInvalid", true),
                            model().attribute("resetSuccess", false));
        }

        @Test
        @DisplayName("password not matching regex sets newPasswordError")
        void weakPassword() throws Exception {
            when(jwtService.extractSubject(TOKEN)).thenReturn(EMAIL);
            when(userService.findUserByEmail(EMAIL)).thenReturn(user);
            when(jwtService.isValid(TOKEN, user)).thenReturn(true);
            when(messageSource.getMessage(eq("password.fail.noRequirements"), any(), eq(LOCALE)))
                    .thenReturn("Password does not meet requirements.");

            mockMvc
                    .perform(post("/auth/reset-password")
                            .param("token", TOKEN)
                            .param("newPassword", WEAK_PASSWORD)
                            .param("confirmPassword", WEAK_PASSWORD))
                    .andExpect(view().name("auth/reset-password"))
                    .andExpectAll(model().attribute("tokenInvalid", false),
                            model().attribute("resetSuccess", false),
                            model().attributeExists("newPasswordError"));

            verifyNoInteractions(passwordEncoder);
        }

        @Test
        @DisplayName("passwords not matching sets confirmPasswordError")
        void passwordMismatch() throws Exception {
            when(jwtService.extractSubject(TOKEN)).thenReturn(EMAIL);
            when(userService.findUserByEmail(EMAIL)).thenReturn(user);
            when(jwtService.isValid(TOKEN, user)).thenReturn(true);
            when(messageSource.getMessage(eq("password.fail.mismatch"), any(), eq(LOCALE)))
                    .thenReturn("Passwords do not match.");

            mockMvc
                    .perform(post("/auth/reset-password")
                            .param("token", TOKEN)
                            .param("newPassword", VALID_PASSWORD)
                            .param("confirmPassword", WEAK_PASSWORD))
                    .andExpect(view().name("auth/reset-password"))
                    .andExpectAll(model().attribute("tokenInvalid", false),
                            model().attribute("resetSuccess", false),
                            model().attributeExists("confirmPasswordError"));

            verifyNoInteractions(passwordEncoder);
        }

        @Test
        @DisplayName("valid reset updates password, sets resetSuccess=true, clears brute-force")
        void successfulReset() throws Exception {
            when(jwtService.extractSubject(TOKEN)).thenReturn(EMAIL);
            when(userService.findUserByEmail(EMAIL)).thenReturn(user);
            when(jwtService.isValid(TOKEN, user)).thenReturn(true);
            when(passwordEncoder.encode(VALID_PASSWORD)).thenReturn(ENCODED_PASSWORD);

            mockMvc
                    .perform(post("/auth/reset-password")
                            .param("token", TOKEN)
                            .param("newPassword", VALID_PASSWORD)
                            .param("confirmPassword", VALID_PASSWORD))
                    .andExpect(view().name("auth/reset-password"))
                    .andExpectAll(model().attribute("tokenInvalid", false),
                            model().attribute("resetSuccess", true));

            verify(userService).updatePassword(user, ENCODED_PASSWORD);
            verify(bruteForceProtectionService).loginSucceeded(EMAIL);
        }

        @Test
        @DisplayName("token is always present in model on any non-invalid outcome")
        void tokenAlwaysInModel() throws Exception {
            when(jwtService.extractSubject(TOKEN)).thenReturn(EMAIL);
            when(userService.findUserByEmail(EMAIL)).thenReturn(user);
            when(jwtService.isValid(TOKEN, user)).thenReturn(true);
            when(passwordEncoder.encode(VALID_PASSWORD)).thenReturn(ENCODED_PASSWORD);

            mockMvc
                    .perform(post("/auth/reset-password")
                            .param("token", TOKEN)
                            .param("newPassword", VALID_PASSWORD)
                            .param("confirmPassword", VALID_PASSWORD))
                    .andExpect(view().name("auth/reset-password"))
                    .andExpect(model().attribute("token", TOKEN));
        }
    }
}
