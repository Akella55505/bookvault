package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.model.Client;
import com.epam.rd.autocode.spring.project.model.User;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JwtService")
public class JwtServiceTest {
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(new CookieService());
        jwtService.setSecret("PFUvgkjpBJ791Y8mkHvFfK5yxgrxmb5WvxPfpQlzBvw=");
        jwtService.setExpirationTime(TimeUnit.DAYS.toMillis(1));
    }

    @Nested
    @DisplayName("generates token")
    class GenerateToken {
        @Test
        @DisplayName("from UserDetails object")
        void fromUserDetails() {
            User user = Client.builder().email("email").build();
            String token = jwtService.generateToken(user);

            assertTrue(jwtService.isValid(token, user));
            assertEquals(user.getEmail(), jwtService.extractSubject(token));
        }

        @Test
        @DisplayName("from email string")
        void fromEmail() {
            String email = "email";
            String token = jwtService.generateToken(email);

            assertEquals(email, jwtService.extractSubject(token));
        }
    }

    @Test
    @DisplayName("checks if token is valid")
    void checksIsValid() {
        String email = "email";
        User wrongUser = Client.builder().email("error").build();

        String token = jwtService.generateToken(email);

        assertFalse(jwtService.isValid(token, wrongUser));
    }

    @Test
    @DisplayName("creates cookie with provided jwt")
    void createsCookie() {
        String email = "email";
        String token = jwtService.generateToken(email);

        Cookie cookie = jwtService.createCookie(token);

        assertEquals("jwt", cookie.getName());
        assertEquals(token, cookie.getValue());
    }
}
