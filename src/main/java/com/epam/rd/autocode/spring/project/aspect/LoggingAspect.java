package com.epam.rd.autocode.spring.project.aspect;

import com.epam.rd.autocode.spring.project.model.User;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class LoggingAspect {
    @Pointcut("@within(org.springframework.stereotype.Controller)")
    public void controllers() {}

    @After("controllers()")
    public void logControllerCall(JoinPoint jp) {
        String className = jp.getSignature().getDeclaringType().getSimpleName();
        String method = jp.getSignature().getName();

        Object principal = currentUser();

        log.debug("{}.{} user={}", className, method, formatPrincipal(principal));
    }

    private Object currentUser() {
        Authentication auth = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }

        return auth.getPrincipal();
    }

    private String formatPrincipal(Object principal) {
        if (principal instanceof User user) {
            return user.getEmail();
        }

        return String.valueOf(principal);
    }
}
