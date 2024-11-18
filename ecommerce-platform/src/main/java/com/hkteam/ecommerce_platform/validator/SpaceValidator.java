package com.hkteam.ecommerce_platform.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class SpaceValidator implements ConstraintValidator<ValidSpace, String> {

    @Override
    public void initialize(ValidSpace constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return (!value.isEmpty()) && (value.trim().length() == value.length());
    }
}
