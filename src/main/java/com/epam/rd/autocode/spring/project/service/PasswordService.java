package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.model.PasswordChangeToken;
import com.epam.rd.autocode.spring.project.model.User;
import com.epam.rd.autocode.spring.project.repo.PasswordChangeTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PasswordService {
    private final PasswordChangeTokenRepository passwordChangeTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public static final String PASSWORD_REGEX = "^(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$";

    public Boolean checkPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    public String createAndSavePasswordChangeToken(User user, String newPassword) {
        Optional<PasswordChangeToken> changeToken = passwordChangeTokenRepository.findByUserEmail(user.getEmail());
        changeToken.ifPresent(passwordChangeTokenRepository::delete);

        String token = jwtService.generateToken(user);

        PasswordChangeToken passwordChangeToken = PasswordChangeToken.builder()
                .userEmail(user.getEmail())
                .newPassword(passwordEncoder.encode(newPassword))
                .token(token)
                .build();
        passwordChangeTokenRepository.save(passwordChangeToken);

        return token;
    }
}
