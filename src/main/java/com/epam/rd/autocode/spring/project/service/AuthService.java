package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.dto.RegistrationRequestDTO;
import com.epam.rd.autocode.spring.project.model.Client;
import com.epam.rd.autocode.spring.project.model.User;
import com.epam.rd.autocode.spring.project.repo.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final ClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;

    public String login(String email, String password) {
        Authentication auth = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
        User user = (User) auth.getPrincipal();
        return jwtService.generateToken(user);
    }

    public String register(RegistrationRequestDTO registrationRequestDTO) {
        Client client = clientRepository.save(Client.builder()
                .name(registrationRequestDTO.getName())
                .email(registrationRequestDTO.getEmail())
                .password(passwordEncoder.encode(registrationRequestDTO.getPassword()))
                .build());
        return jwtService.generateToken(client);
    }
}
