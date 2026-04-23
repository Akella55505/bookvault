package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.dto.EmployeeDTO;
import com.epam.rd.autocode.spring.project.dto.UserDTO;
import com.epam.rd.autocode.spring.project.model.User;
import com.epam.rd.autocode.spring.project.model.enums.Role;
import com.epam.rd.autocode.spring.project.service.*;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.Locale;

@Controller
@RequiredArgsConstructor
@RequestMapping("/profile")
public class UserController {
    private final UserService userService;
    private final JwtService jwtService;
    private final PasswordService passwordService;
    private final EmailService emailService;
    private final MessageSource messageSource;

    @GetMapping
    public ModelAndView profile(@AuthenticationPrincipal User user) {
        ModelAndView mav = new ModelAndView("profile");

        mav.addObject("user", userService.mapUserToDTO(user));

        Role userRole = userService.getRole(user);
        UserDTO userDTO;
        if (userRole == Role.CLIENT) {
            userDTO = new ClientDTO();
        } else {
            userDTO = new EmployeeDTO();
        }

        mav.addObject("userDTO", userDTO);
        return mav;
    }

    @PostMapping("/update")
    public String updateProfile(@AuthenticationPrincipal User user,
                                @ModelAttribute UserDTO userDTO,
                                HttpServletResponse response) {
        String userEmail = user.getEmail();
        user = userService.updateUser(user, userDTO);

        if (!userDTO.getEmail().isBlank() && !userEmail.equals(userDTO.getEmail())) {
            String jwt = jwtService.generateToken(user);
            response.addCookie(jwtService.createCookie(jwt));
        }
        return "redirect:/profile";
    }

    @GetMapping("/change-password")
    public ModelAndView changePasswordPage(@AuthenticationPrincipal User user) {
        ModelAndView mav = new ModelAndView("profile/change-password");

        UserDTO userDTO = userService.mapUserToDTO(user);
        mav.addObject("user", userDTO);
        return mav;
    }

    @PostMapping("/change-password")
    public ModelAndView changePassword(@AuthenticationPrincipal User user,
                                       @RequestParam String currentPassword,
                                       @RequestParam String newPassword,
                                       @RequestParam String confirmPassword,
                                       Locale locale) {
        ModelAndView mav = new ModelAndView("profile/change-password");

        UserDTO userDTO = userService.mapUserToDTO(user);
        mav.addObject("user", userDTO);

        if (!passwordService.checkPassword(currentPassword, user.getPassword())) {
            mav.addObject("currentPasswordError", "Wrong password");
        } else if (currentPassword.equals(newPassword)) {
            String errorMessage = messageSource.getMessage("password.fail.equalToOld", null, locale);
            mav.addObject("newPasswordError", errorMessage);
        } else if (!newPassword.matches(PasswordService.PASSWORD_REGEX)) {
            String errorMessage = messageSource.getMessage("password.fail.noRequirements", null, locale);
            mav.addObject("newPasswordError", errorMessage);
        } else if (!newPassword.equals(confirmPassword)) {
            mav.addObject("confirmPasswordError", "Passwords do not match");
        } else {
            String emailSubject = messageSource.getMessage("password.email.subject", null, locale);
            String emailMessage = messageSource.getMessage("password.email.text", null, locale);
            String link = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUri() +
                    "/profile/change-password/verify?token=" +
                    passwordService.createAndSavePasswordChangeToken(user, newPassword);

            String successMessage = messageSource.getMessage("password.success.sent", null, locale);
            mav.addObject("successMessage", successMessage);
            emailService.sendVerificationEmail(user.getEmail(), emailSubject,
                    emailMessage,
                    link);
        }

        return mav;
    }

    @GetMapping("/change-password/verify")
    public ModelAndView changePasswordVerify(@AuthenticationPrincipal User user,
                                             @RequestParam String token,
                                             Locale locale) {
        ModelAndView mav = new ModelAndView("profile/change-password");
        UserDTO userDTO = userService.mapUserToDTO(user);
        mav.addObject("user", userDTO);

        try {
            jwtService.isValid(token, user);
            userService.updatePasswordByToken(user, token);
        } catch (Exception e) {
            mav.addObject("errorMessageExpired", "The link has expired. Try again");
            return mav;
        }

        String successMessage = messageSource.getMessage("password.success.verify", null, locale);
        mav.addObject("successMessage", successMessage);

        return mav;
    }
}
