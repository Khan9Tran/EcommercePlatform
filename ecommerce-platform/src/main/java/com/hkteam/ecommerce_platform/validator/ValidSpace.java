package com.hkteam.ecommerce_platform.validator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Constraint(validatedBy = SpaceValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidSpace {
    String message() default "INVALID_SPACE";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
