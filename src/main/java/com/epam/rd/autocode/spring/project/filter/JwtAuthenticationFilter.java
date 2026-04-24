package com.epam.rd.autocode.spring.project.filter;

import com.epam.rd.autocode.spring.project.model.RefreshToken;
import com.epam.rd.autocode.spring.project.service.CookieService;
import com.epam.rd.autocode.spring.project.service.JwtService;
import com.epam.rd.autocode.spring.project.service.RefreshTokenService;
import com.epam.rd.autocode.spring.project.service.UserService;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

@Profile("!test")
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserService userService;
    private final RefreshTokenService refreshTokenService;
    private final CookieService cookieService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        final Cookie[] cookies = request.getCookies();
        Optional<Cookie> optionalJwtCookie;
        Optional<Cookie> optionalRefreshCookie;
        if (cookies == null) {
            optionalJwtCookie = Optional.empty();
            optionalRefreshCookie = Optional.empty();
        } else {
            optionalJwtCookie = Arrays.stream(cookies).filter(cookie -> cookie.getName().equals("jwt")).findFirst();
            optionalRefreshCookie = Arrays.stream(cookies).filter(cookie -> cookie.getName().equals("refresh")).findFirst();
        }
        String jwt = null;
        String email = null;

        if (optionalJwtCookie.isPresent()) {
            jwt = optionalJwtCookie.get().getValue();
            try {
                email = jwtService.extractSubject(jwt);
            } catch (ExpiredJwtException e) {
                var refreshToken = getValidRefreshToken(optionalRefreshCookie);

                if (refreshToken != null) {
                    email = refreshToken.getUserEmail();
                    jwt = jwtService.generateToken(refreshToken.getUserEmail());
                    response.addCookie(jwtService.createCookie(jwt));
                } else {
                    Cookie cookie = cookieService.createCookie("jwt", "", 0);
                    response.addCookie(cookie);
                    filterChain.doFilter(request, response);
                    return;
                }
            }
        }

        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails;
            try {
                userDetails = userService.findUserByEmail(email);
            } catch (UsernameNotFoundException e) {
                filterChain.doFilter(request, response);
                return;
            }
            authenticateRequest(request, jwt, userDetails);
        }

        filterChain.doFilter(request, response);
    }

    private void authenticateRequest(@NonNull HttpServletRequest request, String jwt, UserDetails userDetails) {
        if (jwtService.isValid(jwt, userDetails)) {
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userDetails,
                    null,
                    userDetails.getAuthorities());
            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        }
    }

    private RefreshToken getValidRefreshToken(Optional<Cookie> optionalRefreshCookie) {
        String refresh;
        if (optionalRefreshCookie.isPresent()) {
            refresh = optionalRefreshCookie.get().getValue();
            var refreshToken = refreshTokenService.getTokenByToken(refresh).orElse(null);

            if (refreshToken != null) {
                boolean refreshIsExpired = refreshTokenService.isExpired(refreshToken);

                if (!refreshIsExpired) {
                    return refreshToken;
                }
            }
        }
        return null;
    }
}
