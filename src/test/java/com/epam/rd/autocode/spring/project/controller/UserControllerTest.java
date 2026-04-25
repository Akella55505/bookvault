package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.dto.EmployeeDTO;
import com.epam.rd.autocode.spring.project.dto.UserDTO;
import com.epam.rd.autocode.spring.project.model.User;
import com.epam.rd.autocode.spring.project.model.enums.Role;
import com.epam.rd.autocode.spring.project.service.EmailService;
import com.epam.rd.autocode.spring.project.service.JwtService;
import com.epam.rd.autocode.spring.project.service.PasswordService;
import com.epam.rd.autocode.spring.project.service.UserService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Locale;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
@DisplayName("UserController")
public class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;
    @MockBean
    private JwtService jwtService;
    @MockBean
    private PasswordService passwordService;
    @MockBean
    private EmailService emailService;
    @MockBean
    private MessageSource messageSource;

    private static final User CLIENT = SecurityConfig.CLIENT;
    private static final User EMPLOYEE = SecurityConfig.EMPLOYEE;

    private static final String NEW_EMAIL = "new@example.com";
    private static final String CURRENT_PW = "OldSecret1!";
    private static final String NEW_PW = "NewSecret1!";
    private static final String WEAK_PW = "weak";
    private static final String JWT = "header.payload.sig";
    private static final String TOKEN = "verify.jwt.token";

    private ClientDTO clientDTO;
    private EmployeeDTO employeeDTO;

    @BeforeEach
    void setUp() {
        clientDTO = new ClientDTO();
        employeeDTO = new EmployeeDTO();
    }

    @Nested
    @DisplayName("GET /profile")
    class ProfilePage {

        @Test
        @DisplayName("client sees profile view with ClientDTO in model")
        void clientProfile() throws Exception {
            when(userService.mapUserToDTO(CLIENT)).thenReturn(clientDTO);
            when(userService.getRole(CLIENT)).thenReturn(Role.CLIENT);

            mockMvc.perform(get("/profile").with(user(CLIENT)))
                    .andExpect(status().isOk())
                    .andExpect(view().name("profile"))
                    .andExpect(model().attribute("user", clientDTO))
                    .andExpect(model().attributeExists("userDTO"));
        }

        @Test
        @DisplayName("employee sees profile view with EmployeeDTO in model")
        void employeeProfile() throws Exception {
            when(userService.mapUserToDTO(EMPLOYEE)).thenReturn(employeeDTO);
            when(userService.getRole(EMPLOYEE)).thenReturn(Role.EMPLOYEE);

            mockMvc.perform(get("/profile").with(user(EMPLOYEE)))
                    .andExpect(status().isOk())
                    .andExpect(view().name("profile"))
                    .andExpect(model().attribute("user", employeeDTO))
                    .andExpect(model().attributeExists("userDTO"));
        }
    }

    @Nested
    @DisplayName("POST /profile/update")
    class UpdateProfile {

        @Test
        @DisplayName("email unchanged — no new JWT cookie, redirects to /profile")
        void emailUnchanged() throws Exception {
            clientDTO.setEmail(CLIENT.getEmail());
            when(userService.updateUser(eq(CLIENT), any(UserDTO.class))).thenReturn(CLIENT);

            mockMvc.perform(post("/profile/update")
                            .with(user(CLIENT))
                            .flashAttr("userDTO", clientDTO))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/profile"));

            verifyNoInteractions(jwtService);
        }

        @Test
        @DisplayName("email changed — issues new JWT cookie, redirects to /profile")
        void emailChanged() throws Exception {
            clientDTO.setEmail(NEW_EMAIL);
            Cookie newCookie = new Cookie("jwt", JWT);

            when(userService.updateUser(eq(CLIENT), any(UserDTO.class))).thenReturn(CLIENT);
            when(jwtService.generateToken(CLIENT)).thenReturn(JWT);
            when(jwtService.createCookie(JWT)).thenReturn(newCookie);

            mockMvc.perform(post("/profile/update")
                            .with(user(CLIENT))
                            .flashAttr("userDTO", clientDTO))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/profile"))
                    .andExpect(cookie().value("jwt", JWT));
        }

        @Test
        @DisplayName("blank email in DTO — no new JWT cookie issued")
        void blankEmailInDto() throws Exception {
            clientDTO.setEmail("");
            when(userService.updateUser(eq(CLIENT), any(UserDTO.class))).thenReturn(CLIENT);

            mockMvc.perform(post("/profile/update")
                            .with(user(CLIENT))
                            .flashAttr("userDTO", clientDTO))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/profile"));

            verifyNoInteractions(jwtService);
        }
    }

    @Nested
    @DisplayName("GET /profile/change-password")
    class ChangePasswordPage {

        @Test
        @DisplayName("returns profile/change-password view with user in model")
        void returnsView() throws Exception {
            when(userService.mapUserToDTO(CLIENT)).thenReturn(clientDTO);

            mockMvc.perform(get("/profile/change-password").with(user(CLIENT)))
                    .andExpect(status().isOk())
                    .andExpect(view().name("profile/change-password"))
                    .andExpect(model().attribute("user", clientDTO));
        }
    }

    @Nested
    @DisplayName("POST /profile/change-password")
    class ChangePassword {

        @BeforeEach
        void setUp() {
            when(userService.mapUserToDTO(CLIENT)).thenReturn(clientDTO);
        }

        @Test
        @DisplayName("wrong current password sets currentPasswordError")
        void wrongCurrentPassword() throws Exception {
            when(passwordService.checkPassword(CURRENT_PW, CLIENT.getPassword()))
                    .thenReturn(false);

            mockMvc.perform(post("/profile/change-password")
                            .with(user(CLIENT))
                            .param("currentPassword", CURRENT_PW)
                            .param("newPassword", NEW_PW)
                            .param("confirmPassword", NEW_PW))
                    .andExpect(status().isOk())
                    .andExpect(view().name("profile/change-password"))
                    .andExpect(model().attributeExists("currentPasswordError"))
                    .andExpect(model().attribute("user", clientDTO));

            verifyNoInteractions(emailService);
        }

        @Test
        @DisplayName("new password same as current sets newPasswordError")
        void newSameAsCurrent() throws Exception {
            when(passwordService.checkPassword(CURRENT_PW, CLIENT.getPassword()))
                    .thenReturn(true);
            when(messageSource.getMessage(eq("password.fail.equalToOld"), any(), any(Locale.class)))
                    .thenReturn("New password must differ from the old one.");

            mockMvc.perform(post("/profile/change-password")
                            .with(user(CLIENT))
                            .param("currentPassword", CURRENT_PW)
                            .param("newPassword", CURRENT_PW)
                            .param("confirmPassword", CURRENT_PW))
                    .andExpect(status().isOk())
                    .andExpect(view().name("profile/change-password"))
                    .andExpect(model().attributeExists("newPasswordError"));

            verifyNoInteractions(emailService);
        }

        @Test
        @DisplayName("new password fails regex sets newPasswordError")
        void weakNewPassword() throws Exception {
            when(passwordService.checkPassword(CURRENT_PW, CLIENT.getPassword()))
                    .thenReturn(true);
            when(messageSource.getMessage(eq("password.fail.noRequirements"), any(), any(Locale.class)))
                    .thenReturn("Password does not meet requirements.");

            mockMvc.perform(post("/profile/change-password")
                            .with(user(CLIENT))
                            .param("currentPassword", CURRENT_PW)
                            .param("newPassword",     WEAK_PW)
                            .param("confirmPassword", WEAK_PW))
                    .andExpect(status().isOk())
                    .andExpect(view().name("profile/change-password"))
                    .andExpect(model().attributeExists("newPasswordError"));

            verifyNoInteractions(emailService);
        }

        @Test
        @DisplayName("confirmation mismatch sets confirmPasswordError")
        void confirmationMismatch() throws Exception {
            when(passwordService.checkPassword(CURRENT_PW, CLIENT.getPassword()))
                    .thenReturn(true);

            mockMvc.perform(post("/profile/change-password")
                            .with(user(CLIENT))
                            .param("currentPassword", CURRENT_PW)
                            .param("newPassword",     NEW_PW)
                            .param("confirmPassword", "Different1!"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("profile/change-password"))
                    .andExpect(model().attributeExists("confirmPasswordError"));

            verifyNoInteractions(emailService);
        }

        @Test
        @DisplayName("valid change sends verification email and sets successMessage")
        void validChange() throws Exception {
            when(passwordService.checkPassword(CURRENT_PW, CLIENT.getPassword()))
                    .thenReturn(true);
            when(passwordService.createAndSavePasswordChangeToken(CLIENT, NEW_PW))
                    .thenReturn(TOKEN);
            when(messageSource.getMessage(eq("password.email.subject"), any(), any(Locale.class)))
                    .thenReturn("Confirm password change");
            when(messageSource.getMessage(eq("password.email.text"), any(), any(Locale.class)))
                    .thenReturn("Click to confirm");
            when(messageSource.getMessage(eq("password.success.sent"), any(), any(Locale.class)))
                    .thenReturn("Check your email.");

            mockMvc.perform(post("/profile/change-password")
                            .with(user(CLIENT))
                            .param("currentPassword", CURRENT_PW)
                            .param("newPassword",     NEW_PW)
                            .param("confirmPassword", NEW_PW))
                    .andExpect(status().isOk())
                    .andExpect(view().name("profile/change-password"))
                    .andExpect(model().attributeExists("successMessage"))
                    .andExpect(model().attributeDoesNotExist(
                            "currentPasswordError", "newPasswordError", "confirmPasswordError"));

            verify(emailService).sendVerificationEmail(
                    eq(CLIENT.getEmail()), anyString(), anyString(), contains(TOKEN));
        }
    }

    @Nested
    @DisplayName("GET /profile/change-password/verify")
    class ChangePasswordVerify {

        @BeforeEach
        void setUp() {
            when(userService.mapUserToDTO(CLIENT)).thenReturn(clientDTO);
        }

        @Test
        @DisplayName("valid token updates password and sets successMessage")
        void validToken() throws Exception {
            when(jwtService.isValid(TOKEN, CLIENT)).thenReturn(true);
            doNothing().when(userService).updatePasswordByToken(CLIENT, TOKEN);
            when(messageSource.getMessage(eq("password.success.verify"), any(), any(Locale.class)))
                    .thenReturn("Password updated successfully.");

            mockMvc.perform(get("/profile/change-password/verify")
                            .with(user(CLIENT))
                            .param("token", TOKEN))
                    .andExpect(status().isOk())
                    .andExpect(view().name("profile/change-password"))
                    .andExpect(model().attributeExists("successMessage"))
                    .andExpect(model().attribute("user", clientDTO))
                    .andExpect(model().attributeDoesNotExist("errorMessageExpired"));
        }

        @Test
        @DisplayName("isValid throwing sets errorMessageExpired and returns early")
        void expiredToken() throws Exception {
            when(jwtService.isValid(TOKEN, CLIENT))
                    .thenThrow(new RuntimeException("Token expired"));

            mockMvc.perform(get("/profile/change-password/verify")
                            .with(user(CLIENT))
                            .param("token", TOKEN))
                    .andExpect(status().isOk())
                    .andExpect(view().name("profile/change-password"))
                    .andExpect(model().attributeExists("errorMessageExpired"))
                    .andExpect(model().attribute("user", clientDTO));

            verify(userService, never()).updatePasswordByToken(any(), any());
            verify(messageSource, never()).getMessage(eq("password.success.verify"), any(), any());
        }

        @Test
        @DisplayName("updatePasswordByToken throwing sets errorMessageExpired")
        void updateFails() throws Exception {
            when(jwtService.isValid(TOKEN, CLIENT)).thenReturn(true);
            doThrow(new RuntimeException("Update failed"))
                    .when(userService).updatePasswordByToken(CLIENT, TOKEN);

            mockMvc.perform(get("/profile/change-password/verify")
                            .with(user(CLIENT))
                            .param("token", TOKEN))
                    .andExpect(status().isOk())
                    .andExpect(view().name("profile/change-password"))
                    .andExpect(model().attributeExists("errorMessageExpired"));
        }
    }
}
