package com.epam.rd.autocode.spring.project.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Constraint(validatedBy = UniqueEmailValidator.class)
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface UniqueEmail {
    String message() default "{auth.register.fail.emailRegistered}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
