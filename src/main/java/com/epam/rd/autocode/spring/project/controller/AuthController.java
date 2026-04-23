package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.model.RefreshToken;
import com.epam.rd.autocode.spring.project.model.User;
import com.epam.rd.autocode.spring.project.service.*;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.Locale;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;
    private final JwtService jwtService;
    private final MessageSource messageSource;
    private final UserService userService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final BruteForceProtectionService bruteForceProtectionService;

    @GetMapping("/blocked")
    public String blockedPage() {
        return "auth/blocked";
    }

    @GetMapping("/login")
    public ModelAndView loginPage(@RequestParam(required = false) String returnUrl) {
        ModelAndView mav = new ModelAndView("auth/login");

        mav.addObject("returnUrl", returnUrl);
        return mav;
    }

    @PostMapping("/login")
    public ModelAndView login(@RequestParam(required = false) String returnUrl,
                        @RequestParam String email,
                        @RequestParam String password,
                        @RequestParam(defaultValue = "false") Boolean rememberMe,
                        HttpServletResponse response) {
        ModelAndView mav = new ModelAndView("redirect:" + ((returnUrl == null || returnUrl.isBlank()) ? "/" : returnUrl));
        String jwt;

        if (bruteForceProtectionService.isBlocked(email)) {
            mav = new ModelAndView("redirect:/auth/blocked");
            return mav;
        }

        try {
            jwt = authService.login(email, password);
        } catch (Exception e) {
            bruteForceProtectionService.loginFailed(email);
            mav = new ModelAndView("auth/login");
            mav.addObject("returnUrl", returnUrl);
            mav.addObject("errorMessage", "Wrong email or password");
            return mav;
        }

        response.addCookie(jwtService.createCookie(jwt));
        bruteForceProtectionService.loginSucceeded(email);

        if (rememberMe) {
            log.debug("User {} picked \"Remember me\"", email);

            RefreshToken refreshToken = refreshTokenService.createRefreshToken(email);
            response.addCookie(refreshTokenService.createCookie(refreshToken));
        }

        log.info("Successful login by: {}", email);

        return mav;
    }

    @GetMapping("/register")
    public ModelAndView registerPage(@RequestParam(required = false) String returnUrl,
                                     @ModelAttribute ClientDTO clientDTO) {
        ModelAndView mav = new ModelAndView("auth/register");

        mav.addObject("returnUrl", returnUrl);

        return mav;
    }

    @PostMapping("/register")
    public ModelAndView register(@RequestParam(required = false) String returnUrl,
                                 @Valid @ModelAttribute ClientDTO clientDTO,
                                 BindingResult bindingResult,
                                 HttpServletResponse response) {
        ModelAndView mav = new ModelAndView("redirect:" + (returnUrl == null ? "/" : returnUrl));

        if (bindingResult.getFieldError() != null) {
            mav = new ModelAndView("auth/register");
            mav.addObject("returnUrl", returnUrl);
            return mav;
        }

        String jwt = authService.register(clientDTO);
        response.addCookie(jwtService.createCookie(jwt));

        return mav;
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    public ModelAndView forgotPasswordSubmit(@RequestParam String email, Locale locale) {
        ModelAndView mav = new ModelAndView("auth/forgot-password");

        if (userService.checkUserExistsByEmail(email)) {
            log.debug("User {} initiated \"Forgot password\" procedure", email);

            String subject = messageSource.getMessage("forgot.email.subject", null, locale);
            String text= messageSource.getMessage("forgot.email.text", null, locale);
            String link = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUri() +
                    "/auth/reset-password?token=" + jwtService.generateToken(email);
            emailService.sendVerificationEmail(email, subject, text, link);
        }

        mav.addObject("emailSent", true);
        mav.addObject("submittedEmail", email);
        return mav;
    }

    @GetMapping("/reset-password")
    public ModelAndView resetPasswordPage(@RequestParam String token) {
        ModelAndView mav = new ModelAndView("auth/reset-password");
        boolean tokenInvalid = false;

        if (jwtService.isExpired(token)) {
            tokenInvalid = true;
        } else {
            mav.addObject("token", token);
        }

        mav.addObject("tokenInvalid", tokenInvalid);
        mav.addObject("resetSuccess", false);

        return mav;
    }

    @PostMapping("/reset-password")
    public ModelAndView resetPassword(@RequestParam String token,
                                      @RequestParam String newPassword,
                                      @RequestParam String confirmPassword,
                                       Locale locale) {
        ModelAndView mav = new ModelAndView("auth/reset-password");
        User user;
        boolean resetSuccess = false;

        try {
            String email = jwtService.extractSubject(token);
            user = userService.findUserByEmail(email);
            jwtService.isValid(token, user);
        } catch (Exception e) {
            mav.addObject("tokenInvalid", true);
            mav.addObject("resetSuccess", resetSuccess);
            mav.addObject("token", token);
            return mav;
        }

        log.debug("User {} initiated \"Reset password\" procedure", user.getEmail());

        if (!newPassword.matches(PasswordService.PASSWORD_REGEX)) {
            String errorMessage = messageSource.getMessage("password.fail.noRequirements", null, locale);
            mav.addObject("newPasswordError", errorMessage);
        } else if (!newPassword.equals(confirmPassword)) {
            String errorMessage = messageSource.getMessage("password.fail.mismatch", null, locale);
            mav.addObject("confirmPasswordError", errorMessage);
        } else {
            userService.updatePassword(user, passwordEncoder.encode(newPassword));
            resetSuccess = true;
        }

        mav.addObject("resetSuccess", resetSuccess);
        mav.addObject("tokenInvalid", false);
        mav.addObject("token", token);

        bruteForceProtectionService.loginSucceeded(user.getEmail());

        return mav;
    }
}
