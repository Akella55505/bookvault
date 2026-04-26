package com.epam.rd.autocode.spring.project.service;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("CookieService")
public class CookieServiceTest {
    private final CookieService cookieService = new CookieService();

    @Test
    @DisplayName("creates a cookie with provided parameters")
    void createsCookie() {
        Cookie cookie = cookieService.createCookie("cookie", "value", 150);

        assertEquals("cookie", cookie.getName());
        assertEquals("value", cookie.getValue());
        assertEquals(150, cookie.getMaxAge());
        assertTrue(cookie.isHttpOnly());
        assertEquals("Strict", cookie.getAttribute("SameSite"));
        assertTrue(cookie.getSecure());
    }
}
